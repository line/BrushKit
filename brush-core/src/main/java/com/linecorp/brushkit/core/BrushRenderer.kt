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

package com.linecorp.brushkit.core

import android.graphics.Bitmap
import android.graphics.Path
import androidx.annotation.ColorInt
import com.linecorp.brushkit.core.model.DabRenderInfo

/**
 * An interface defines the rendering capabilities for a brush-based drawing tool.
 */
interface BrushRenderer {
    /**
     * Clears the drawing surface, removing all previously rendered content.
     */
    fun clear()

    /**
     * Renders a single point on the drawing surface.
     *
     * @param x The x-coordinate of the point.
     * @param y The y-coordinate of the point.
     * @param color The color of the point.
     * @param size The size of the point.
     */
    fun renderPoint(
        x: Float,
        y: Float,
        @ColorInt color: Int,
        size: Float,
    )

    /**
     * Renders a path on the drawing surface.
     *
     * @param path The path to be rendered.
     * @param color The color of the path.
     * @param size The stroke width of the path.
     */
    fun renderPath(
        path: Path,
        @ColorInt color: Int,
        size: Float,
    )

    /**
     * Renders a point using an eraser, effectively removing content from the drawing surface.
     *
     * @param x The x-coordinate of the eraser point.
     * @param y The y-coordinate of the eraser point.
     * @param size The size of the eraser point.
     */
    fun renderEraserPoint(
        x: Float,
        y: Float,
        size: Float,
    )

    /**
     * Renders a path using an eraser, effectively removing content along the path on the drawing surface.
     *
     * @param path The path of the eraser.
     * @param size The width of the eraser path.
     */
    fun renderEraserPath(
        path: Path,
        size: Float,
    )

    /**
     * Renders a bitmap on the drawing surface.
     *
     * @param bitmap The bitmap to be rendered.
     * @param left The left coordinate where the bitmap starts.
     * @param top The top coordinate where the bitmap starts.
     */
    fun renderBitmap(
        bitmap: Bitmap,
        left: Float,
        top: Float,
    )

    /**
     * Renders the current state of this renderer onto another renderer, offset by the specified coordinates.
     * This can be used to composite multiple layers into one layer.
     *
     * @param renderer The target renderer where the current state will be rendered.
     * @param left The left offset at which to start rendering the state.
     * @param top The top offset at which to start rendering the state.
     */
    fun renderCurrentStateTo(
        renderer: BrushRenderer,
        left: Float,
        top: Float,
    )

    /**
     * Sets the bitmap image and tint color to be used for rendering dabs with [renderDabList].
     *
     * @param bitmap The bitmap image to be used for the dab.
     * @param tintColor The tint color to be applied to the bitmap.
     */
    fun setDabBitmap(
        bitmap: Bitmap,
        @ColorInt tintColor: Int,
    )

    /**
     * Renders a list of brush dabs.
     *
     * @param dabList A list of [DabRenderInfo] objects, each containing the necessary information to render a single dab.
     */
    fun renderDabList(
        dabList: List<DabRenderInfo>,
    )
}
