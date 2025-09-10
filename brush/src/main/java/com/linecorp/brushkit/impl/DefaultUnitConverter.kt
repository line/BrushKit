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

import android.util.DisplayMetrics
import android.util.TypedValue
import com.linecorp.brushkit.core.UnitConverter

/**
 * Default implementation of [UnitConverter] that uses the system's display metrics.
 */
class DefaultUnitConverter(private val displayMetrics: DisplayMetrics) : UnitConverter {
    override fun pxToMillimeter(px: Float): Float =
        px / TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1f, displayMetrics)

    override fun dpToPx(dp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics)
}
