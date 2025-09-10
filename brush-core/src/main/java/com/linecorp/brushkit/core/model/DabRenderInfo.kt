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

import androidx.annotation.FloatRange

/**
 * A class represents information required for rendering a brush's "dab"
 */
data class DabRenderInfo(
    val x: Float,
    val y: Float,
    @FloatRange(from = 0.0) val scale: Float,
    @FloatRange(from = -180.0, to = 180.0) val rotation: Float,
    @FloatRange(from = 0.0, to = 1.0) val alpha: Float,
)
