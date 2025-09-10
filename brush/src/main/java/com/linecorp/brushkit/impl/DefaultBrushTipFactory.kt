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

package com.linecorp.brushkit.impl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.Size
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.appcompat.content.res.AppCompatResources
import com.linecorp.brushkit.core.BrushTipFactory
import com.linecorp.brushkit.extension.ScaleType
import com.linecorp.brushkit.extension.getScaleFactor

/**
 * Default implementation of [BrushTipFactory] that creates bitmap brush tips.
 */
class DefaultBrushTipFactory(
    private val context: Context,
) : BrushTipFactory {
    override fun createBitmapTip(
        resId: Int,
        sizePx: Int,
    ): Bitmap {
        require(sizePx > 0) {
            "size must be greater than 0"
        }
        val drawable = AppCompatResources.getDrawable(context, resId)
        require(drawable != null) {
            "Drawable not found"
        }
        require(drawable.intrinsicWidth > 0 && drawable.intrinsicHeight > 0) {
            "Drawable must have positive width and height"
        }
        val intrinsicSize = Size(drawable.intrinsicWidth, drawable.intrinsicHeight)
        val scaleFactor = intrinsicSize.getScaleFactor(Size(sizePx, sizePx), ScaleType.Fit)
        val scaledWidth = (drawable.intrinsicWidth * scaleFactor).toInt()
        val scaledHeight = (drawable.intrinsicHeight * scaleFactor).toInt()
        val bitmap = createBitmap(
            scaledWidth,
            scaledHeight,
            Bitmap.Config.ARGB_8888,
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    override fun createSoftCircleTip(
        @IntRange(from = 1) sizePx: Int,
        @FloatRange(from = 0.0, to = 1.0) hardness: Float,
    ): Bitmap {
        require(sizePx > 0) { "size must be greater than 0" }
        require(hardness in 0f..1f) { "hardness must be in range 0 to 1" }
        val actualRadius = sizePx / 2f
        val centerPoint = PointF(sizePx / 2f, sizePx / 2f)
        val blur =
            (1f - hardness) * MAX_BLUR_TO_RADIUS_RATIO // the blur range is from 0f to 0.6f of the radius
        val blurRadius = actualRadius * blur
        val solidRadius = actualRadius * (1f - blur)
        val paint = Paint().apply {
            style = Paint.Style.FILL
            if (blurRadius > 0f) {
                maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
            }
        }
        val bitmap = createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawCircle(centerPoint.x, centerPoint.y, solidRadius, paint)
        return bitmap
    }

    companion object {
        private const val MAX_BLUR_TO_RADIUS_RATIO = 0.6f
    }
}
