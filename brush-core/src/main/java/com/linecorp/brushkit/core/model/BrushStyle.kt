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

package com.linecorp.brushkit.core.model

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

/**
 * An interface represents different styles of brush.
 */
sealed interface BrushStyle {
    /**
     * An eraser brush.
     */
    data class Eraser(
        val initialSizeDp: Float,
    ) : BrushStyle

    /**
     * A brush that has solid circle tip and can only change the size and the color.
     */
    data class SolidCircle(
        val initialSizeDp: Float,
        @ColorInt val initialColor: Int,
    ) : BrushStyle

    /**
     * A brush that has soft circle tip and can change the size, the color, and the hardness.
     */
    data class SoftCircle(
        val initialSizeDp: Float,
        @ColorInt val initialColor: Int,
        val initialHardness: Float,
    ) : BrushStyle

    /**
     * A brush with the tip made from an image.
     */
    data class Image(
        @DrawableRes val imageResId: Int,
        val initialSizeDp: Float,
        @ColorInt val initialColor: Int,
    ) : BrushStyle
}
