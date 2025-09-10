//
// Copyright 2025 LY Corporation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.linecorp.brushkit

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.util.Size
import androidx.annotation.ColorInt
import com.linecorp.brushkit.core.BrushRenderer
import com.linecorp.brushkit.core.model.DabRenderInfo

/**
 * A [BrushRenderer] implementation that renders to a [Bitmap] using a [Canvas].
 */
internal class CanvasRenderer(
    private val onRendered: () -> Unit,
) : BrushRenderer {
    private val renderCanvas: Canvas = Canvas()
    var renderBitmap: Bitmap? = null
        set(value) {
            field = value
            renderCanvas.setBitmap(value)
        }

    private val renderLineAndPointPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isAntiAlias = true
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
    }

    private val renderBitmapPaint = Paint().apply {
        isAntiAlias = true
        isDither = true
    }

    private val renderEraserPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private var renderDabBitmapSize: Size = Size(0, 0)
    private val renderDabBitmapMatrix = Matrix()
    private val renderDabPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
    }

    private inline fun render(action: Canvas.() -> Unit) {
        renderCanvas.action()
        onRendered.invoke()
    }

    override fun clear() = render {
        drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
    }

    override fun renderPoint(
        x: Float,
        y: Float,
        @ColorInt color: Int,
        size: Float,
    ) = render {
        renderLineAndPointPaint.apply {
            this.color = color
            this.strokeWidth = size
        }
        drawPoint(x, y, renderLineAndPointPaint)
    }

    override fun renderPath(
        path: Path,
        @ColorInt color: Int,
        size: Float,
    ) = render {
        renderLineAndPointPaint.apply {
            this.color = color
            this.strokeWidth = size
        }
        drawPath(path, renderLineAndPointPaint)
    }

    override fun renderEraserPoint(
        x: Float,
        y: Float,
        size: Float,
    ) = render {
        renderEraserPaint.apply {
            strokeWidth = size
        }
        drawPoint(x, y, renderEraserPaint)
    }

    override fun renderEraserPath(
        path: Path,
        size: Float,
    ) = render {
        renderEraserPaint.apply {
            strokeWidth = size
        }
        drawPath(path, renderEraserPaint)
    }

    override fun renderBitmap(
        bitmap: Bitmap,
        left: Float,
        top: Float,
    ) = render {
        drawBitmap(bitmap, left, top, renderBitmapPaint)
    }

    override fun renderCurrentStateTo(
        renderer: BrushRenderer,
        left: Float,
        top: Float,
    ) {
        renderBitmap?.let { renderer.renderBitmap(bitmap = it, left = left, top = top) }
    }

    override fun setDabBitmap(
        bitmap: Bitmap,
        @ColorInt tintColor: Int,
    ) {
        renderDabPaint.apply {
            this.shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            this.colorFilter = PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_ATOP)
        }
        renderDabBitmapSize = Size(bitmap.width, bitmap.height)
    }

    override fun renderDabList(dabList: List<DabRenderInfo>) = render {
        if (renderDabBitmapSize.width == 0 || renderDabBitmapSize.height == 0) {
            throw IllegalStateException("Dab bitmap size is not set")
        }
        val width = renderDabBitmapSize.width.toFloat()
        val height = renderDabBitmapSize.height.toFloat()
        val pivotX = width / 2f
        val pivotY = height / 2f

        // Represent 2 triangles forming the dab bitmap rectangle:
        // (bottom-left, bottom-right, top-left) and (top-left, bottom-right, top-right)
        val initialVertices =
            floatArrayOf(0f, 0f, width, 0f, 0f, height, 0f, height, width, 0f, width, height)
        val initialTextureCoordinates =
            floatArrayOf(0f, 0f, width, 0f, 0f, height, 0f, height, width, 0f, width, height)

        // Calculate array list of vertices, texture coordinates, and colors for rendering
        val vertices = FloatArray(VERTEX_VALUE_PER_DAB_COUNT * dabList.size)
        val textureCoordinates = FloatArray(TEXTURE_VALUE_PER_DAB_COUNT * dabList.size)
        val hasAlpha = dabList.any { it.alpha < 1f }
        val colors = if (hasAlpha) {
            IntArray(COLOR_VALUE_PER_DAB_COUNT * dabList.size)
        } else {
            null
        }
        dabList.forEachIndexed { dabIndex, dabRenderInfo ->
            renderDabBitmapMatrix.apply {
                reset()
                setTranslate(dabRenderInfo.x - pivotX, dabRenderInfo.y - pivotY)
                preRotate(dabRenderInfo.rotation, pivotX, pivotY)
                preScale(dabRenderInfo.scale, dabRenderInfo.scale, pivotX, pivotY)
            }
            val vertexIndexOffset = dabIndex * VERTEX_VALUE_PER_DAB_COUNT
            renderDabBitmapMatrix.mapPoints(vertices, vertexIndexOffset, initialVertices, 0, 6)

            val textureIndexOffset = dabIndex * TEXTURE_VALUE_PER_DAB_COUNT
            initialTextureCoordinates.forEachIndexed { index, value ->
                textureCoordinates[textureIndexOffset + index] = value
            }

            if (colors != null) {
                val colorIndexOffset = dabIndex * COLOR_VALUE_PER_DAB_COUNT
                for (index in 0 until COLOR_VALUE_PER_DAB_COUNT) {
                    colors[colorIndexOffset + index] =
                        Color.valueOf(1f, 1f, 1f, dabRenderInfo.alpha).toArgb()
                }
            }
        }
        drawVertices(
            // VertexMode
            Canvas.VertexMode.TRIANGLES,
            // vertexCount
            vertices.size,
            // verts
            vertices,
            // vertOffset
            0,
            // texs
            textureCoordinates,
            // texOffset
            0,
            // colors
            colors,
            // colorOffset
            0,
            // indices
            null,
            // indexOffset
            0,
            // indexCount
            0,
            // paint
            renderDabPaint,
        )
    }

    companion object {
        /**
         * 6 points represents 2 triangles forming the dab bitmap rectangle
         */
        private const val POINT_PER_DAB_COUNT = 6

        /**
         * 12 vertex values represents 6 points forming the dab bitmap rectangle
         */
        private const val VERTEX_VALUE_PER_DAB_COUNT = POINT_PER_DAB_COUNT * 2

        /**
         * 12 texture values represents 6 points forming the dab bitmap rectangle
         */
        private const val TEXTURE_VALUE_PER_DAB_COUNT = POINT_PER_DAB_COUNT * 2

        /**
         * 6 color values that corresponds to 6 points forming the dab bitmap rectangle
         */
        private const val COLOR_VALUE_PER_DAB_COUNT = POINT_PER_DAB_COUNT
    }
}
