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

package com.linecorp.brushkit.core.engine.line

import android.graphics.Color
import android.graphics.Path
import android.graphics.PointF
import com.linecorp.brushkit.core.BrushRenderer
import com.linecorp.brushkit.core.engine.BrushEngine
import com.linecorp.brushkit.core.model.Brush
import com.linecorp.brushkit.core.model.BrushSetting
import com.linecorp.brushkit.core.model.BrushStyle

private val INVALID_POINT = PointF(Float.MIN_VALUE, Float.MIN_VALUE)

/**
 * A brush engine that renders brush strokes by drawing lines between points.
 *
 * @param allBrushesRenderer The renderer used to render all brushes.
 * @param drawingBrushRenderer The renderer used to render the current brush.
 * @param brush The brush to be used for rendering.
 */
internal class LineBrushEngine(
    private val allBrushesRenderer: BrushRenderer,
    private val drawingBrushRenderer: BrushRenderer,
    private val brush: Brush,
    setting: BrushSetting,
) : BrushEngine {
    private val size: Float = setting.sizePx

    /**
     * Color used for brush with BrushSetting.Colored setting.
     * The eraser brush does not require this property because it does not have a color.
     */
    private val color: Int = (setting as? BrushSetting.Colored)?.color ?: Color.TRANSPARENT

    private val path: Path = Path()
    private val point0: PointF = PointF()
    private val point1: PointF = PointF()
    private val midPoint: PointF = PointF()

    init {
        require(isBrushSupported(brush)) {
            "Unsupported brush style: ${brush.style}"
        }
    }

    override fun onStrokeDown(
        x: Float,
        y: Float,
        deltaTimeMs: Long,
    ) {
        point0.set(x, y)
        point1.set(INVALID_POINT)
    }

    override fun onStrokeMove(
        x: Float,
        y: Float,
        deltaTimeMs: Long,
    ) {
        if (point1 == INVALID_POINT) {
            point1.set(x, y)
            return
        }

        midPoint.set((x + point1.x) / 2, (y + point1.y) / 2)
        with(path) {
            reset()
            moveTo(point0.x, point0.y)
            quadTo(
                // control point x
                point1.x,
                // control point y
                point1.y,
                // end point x
                midPoint.x,
                // end point y
                midPoint.y,
            )
            renderPath(this)
        }
        point0.set(midPoint)
        point1.set(x, y)
    }

    override fun onStrokeUp(
        x: Float,
        y: Float,
        deltaTimeMs: Long,
    ) {
        if (point1 == INVALID_POINT) {
            renderPoint(x, y)
        } else {
            with(path) {
                reset()
                moveTo(point0.x, point0.y)
                quadTo(
                    // control point x
                    point1.x,
                    // control point y
                    point1.y,
                    // end point x
                    x,
                    // end point y
                    y,
                )
                renderPath(this)
            }
        }

        drawingBrushRenderer.renderCurrentStateTo(allBrushesRenderer, 0f, 0f)
        drawingBrushRenderer.clear()
    }

    private fun renderPath(path: Path) {
        if (brush.style is BrushStyle.SolidCircle) {
            drawingBrushRenderer.renderPath(path, color, size)
        } else if (brush.style is BrushStyle.Eraser) {
            allBrushesRenderer.renderEraserPath(path, size)
        }
    }

    private fun renderPoint(
        x: Float,
        y: Float,
    ) {
        if (brush.style is BrushStyle.SolidCircle) {
            drawingBrushRenderer.renderPoint(x, y, color, size)
        } else if (brush.style is BrushStyle.Eraser) {
            allBrushesRenderer.renderEraserPoint(x, y, size)
        }
    }

    companion object {
        /**
         * Checks if the given brush is supported.
         */
        fun isBrushSupported(brush: Brush): Boolean =
            when (brush.style) {
                is BrushStyle.Eraser,
                is BrushStyle.SolidCircle,
                -> true

                else -> false
            }
    }
}
