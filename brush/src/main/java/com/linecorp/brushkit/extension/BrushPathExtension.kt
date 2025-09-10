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

package com.linecorp.brushkit.extension

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import com.linecorp.brushkit.BrushView
import com.linecorp.brushkit.core.BrushDataProvider
import com.linecorp.brushkit.core.BrushOperationManager
import com.linecorp.brushkit.core.model.BrushPath
import com.linecorp.brushkit.core.model.BrushSetting
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

/**
 * Calculates the bounding rectangle of a list of brush paths.
 *
 * @param brushDataProvider The BrushDataProvider used to retrieve brush data.
 * @return A Rect object representing the bounding box of all brush paths, also taking
 * account of the maximum brush size. Returns `null` if no valid brush paths exist or if all
 * paths are erasers.
 */
fun List<BrushPath>.getBoundingRect(brushDataProvider: BrushDataProvider): Rect? {
    if (this.isEmpty()) return null
    val rect = calculateBoundingRect().also {
        // Expand the bounding rectangle by the maximum brush size value
        val maxSize = maxBrushSize() * maxBrushScale(brushDataProvider)
        it.inset(-maxSize, -maxSize)
    }
    return Rect(
        floor(rect.left).toInt(),
        floor(rect.top).toInt(),
        ceil(rect.right).toInt(),
        ceil(rect.bottom).toInt(),
    )
}

private fun List<BrushPath>.maxBrushScale(brushDataProvider: BrushDataProvider): Float {
    var maxScale = 1f
    this
        .asSequence()
        .mapNotNull { brushDataProvider.getBrush(it.brushId) }
        .flatMap { it.scaleInputConfigs }
        .flatMap { it.mappingPoints }
        .forEach {
            maxScale = max(it.output, maxScale)
        }
    return maxScale
}

private fun List<BrushPath>.maxBrushSize(): Float {
    var maxSize = 0f
    forEach { brushPath ->
        val brushSize = brushPath.setting.sizePx
        maxSize = max(brushSize, maxSize)
    }
    return maxSize
}

private fun List<BrushPath>.calculateBoundingRect(): RectF {
    var left = Float.MAX_VALUE
    var top = Float.MAX_VALUE
    var right = Float.MIN_VALUE
    var bottom = Float.MIN_VALUE

    // Calculate max brush size and the most left, top, right, bottom points
    this
        .filterNot { it.setting is BrushSetting.Eraser }
        .flatMap { it.points }
        .forEach { brushPoint ->
            if (brushPoint.x < left) left = brushPoint.x
            if (brushPoint.x > right) right = brushPoint.x
            if (brushPoint.y < top) top = brushPoint.y
            if (brushPoint.y > bottom) bottom = brushPoint.y
        }
    return RectF(left, top, right, bottom)
}

/**
 * Generates a Bitmap from list of brush paths.
 *
 * @param context The context used to create the Bitmap.
 * @param brushOperationManager The BrushOperationManager used to manage brush operations.
 * @return A Bitmap containing the rendered brush paths, or `null` if no valid brush paths exist.
 */
fun List<BrushPath>.generateBitmap(
    context: Context,
    brushOperationManager: BrushOperationManager,
): Bitmap? {
    val brushDataProvider = brushOperationManager.brushDataProvider
    val boundingRect = getBoundingRect(brushDataProvider) ?: return null

    val viewWidth = boundingRect.right
    val viewHeight = boundingRect.bottom
    val brushView = BrushView(
        context = context,
        brushOperationManager = brushOperationManager,
    )
    brushView.measure(
        View.MeasureSpec.makeMeasureSpec(viewWidth, View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(viewHeight, View.MeasureSpec.EXACTLY),
    )
    brushView.layout(0, 0, brushView.measuredWidth, brushView.measuredHeight)
    brushOperationManager.redraw()
    val brushBitmapWithBounds = brushView.exportBrushBitmapWithBounds()
    return brushBitmapWithBounds?.bitmap
}
