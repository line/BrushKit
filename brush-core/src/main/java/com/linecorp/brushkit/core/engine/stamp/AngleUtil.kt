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

package com.linecorp.brushkit.core.engine.stamp

import androidx.annotation.FloatRange

/**
 * Utility object for handling angle maths.
 */
internal object AngleUtil {
    /**
     * Converts the given angle to the range of -180 to 180 degrees.
     */
    @FloatRange(from = -180.0, to = 180.0)
    fun to180DegreeRange(angle: Float): Float {
        var newAngle = angle % 360
        if (newAngle < -180) {
            newAngle += 360
        } else if (newAngle >= 180) {
            newAngle -= 360
        }
        return newAngle
    }
}
