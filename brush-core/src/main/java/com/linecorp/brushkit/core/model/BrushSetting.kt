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
import androidx.annotation.FloatRange

/**
 * An interface represents settings that the user can perform on each brush style.
 */
sealed interface BrushSetting {
    val sizePx: Float

    fun copyWithSize(sizePx: Float): BrushSetting

    /**
     * Setting for [BrushStyle.Eraser]
     */
    data class Eraser(
        override val sizePx: Float,
    ) : BrushSetting {
        override fun copyWithSize(sizePx: Float): BrushSetting = copy(sizePx = sizePx)
    }

    sealed interface Colored : BrushSetting {
        @get:ColorInt
        val color: Int

        fun copyWithColor(
            @ColorInt color: Int,
        ): BrushSetting

        /**
         * Setting for [BrushStyle.SolidCircle]
         */
        data class SolidCircle(
            override val sizePx: Float,
            @ColorInt override val color: Int,
        ) : Colored {
            override fun copyWithColor(color: Int): BrushSetting = copy(color = color)

            override fun copyWithSize(sizePx: Float): BrushSetting = copy(sizePx = sizePx)
        }

        /**
         * Setting for [BrushStyle.SoftCircle]
         */
        data class SoftCircle(
            override val sizePx: Float,
            @ColorInt override val color: Int,
            @FloatRange(from = 0.0, to = 1.0) val hardness: Float,
        ) : Colored {
            override fun copyWithSize(sizePx: Float): BrushSetting = copy(sizePx = sizePx)

            override fun copyWithColor(color: Int): BrushSetting = copy(color = color)
        }

        /**
         * Setting for [BrushStyle.Image]
         */
        data class Image(
            override val sizePx: Float,
            @ColorInt override val color: Int,
        ) : Colored {
            override fun copyWithSize(sizePx: Float): BrushSetting = copy(sizePx = sizePx)

            override fun copyWithColor(color: Int): BrushSetting = copy(color = color)
        }
    }
}
