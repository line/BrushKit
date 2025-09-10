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

package com.linecorp.brushkit.core.engine

/**
 * An interface defines the essential actions for a brush engine used in drawing operations.
 */
internal interface BrushEngine {
    /**
     * Called when the user starts a stroke.
     *
     * @param x The x-coordinate where the stroke begins.
     * @param y The y-coordinate where the stroke begins.
     * @param deltaTimeMs The time in milliseconds since the last stroke event. This can be used
     *                    to calculate the speed of the stroke.
     */
    fun onStrokeDown(
        x: Float,
        y: Float,
        deltaTimeMs: Long,
    )

    /**
     * Called when the user continues to draw the stroke.
     *
     * @param x The current x-coordinate of the stroke.
     * @param y The current y-coordinate of the stroke.
     * @param deltaTimeMs The time in milliseconds since the last stroke event. This can be used
     *                    to adjust the characteristics of the stroke based on the speed of movement.
     */
    fun onStrokeMove(
        x: Float,
        y: Float,
        deltaTimeMs: Long,
    )

    /**
     * Called when the user ends a stroke by lifting the finger.
     *
     * @param x The x-coordinate where the stroke ends.
     * @param y The y-coordinate where the stroke ends.
     * @param deltaTimeMs The time in milliseconds since the last stroke event. This can be used
     *                    for final adjustments to the stroke's rendering.
     */
    fun onStrokeUp(
        x: Float,
        y: Float,
        deltaTimeMs: Long,
    )
}
