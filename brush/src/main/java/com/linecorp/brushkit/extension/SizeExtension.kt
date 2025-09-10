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

import android.util.Size
import kotlin.math.max
import kotlin.math.min

/**
 * Scale types for scaling a source size within a target space.
 */
internal enum class ScaleType {
    Fit,
    Fill,
}

/**
 * Gets the scale factor required to scale the source size within the target space.
 *
 * @param targetSpace The target space to scale the source size within.
 * @param scaleType The type of scaling to apply.
 */
internal fun Size.getScaleFactor(
    targetSpace: Size,
    scaleType: ScaleType,
): Float {
    require(width > 0 && height > 0) {
        "The source size must be positive."
    }
    val widthScale = targetSpace.width / width.toFloat()
    val heightScale = targetSpace.height / height.toFloat()
    return when (scaleType) {
        ScaleType.Fit -> min(widthScale, heightScale)
        ScaleType.Fill -> max(widthScale, heightScale)
    }
}
