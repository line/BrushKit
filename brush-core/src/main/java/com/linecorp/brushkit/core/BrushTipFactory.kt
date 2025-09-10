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
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.annotation.IntRange

/**
 * An interface for creating different types of brush tips.
 */
interface BrushTipFactory {
    /**
     * Creates a bitmap brush tip from the specified drawable resource.
     */
    fun createBitmapTip(
        @DrawableRes resId: Int,
        @IntRange(from = 1) sizePx: Int,
    ): Bitmap

    /**
     * Creates a soft circle brush tip with the specified size and hardness.
     */
    fun createSoftCircleTip(
        @IntRange(from = 1) sizePx: Int,
        @FloatRange(from = 0.0, to = 1.0) hardness: Float,
    ): Bitmap
}
