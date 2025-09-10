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

/**
 * A class representing a single point in a brush stroke.
 *
 * @property x The x-coordinate of the brush point on the drawing surface.
 * @property y The y-coordinate of the brush point on the drawing surface.
 * @property deltaTimeMs The time in milliseconds since the last point was recorded,
 *                       useful for calculations involving the speed or fluidity of the stroke.
 */
data class BrushPoint(
    val x: Float,
    val y: Float,
    val deltaTimeMs: Long,
)
