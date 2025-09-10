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
import com.linecorp.brushkit.core.engine.stamp.StampBrushEngine

/**
 * An interface represents different types of inputs for [StampBrushEngine].
 */
sealed interface BrushInputConfig {
    val mappingPoints: List<MappingPoint>

    /**
     * Configuration for speed-based brush input.
     *
     * @property smoothingFactor A float value representing the smoothing factor, constrained between 0.0 and 1.0.
     * @property mappingPoints A list of MappingPoint objects that define the input-output mapping for speed.
     *                         The input represents the speed with the value is constrained between 0.0 and 1.0.
     *                         In which, 0.0 represents the slowest speed and 1.0 represents the fastest speed.
     */
    class Speed(
        @FloatRange(from = 0.0, to = 1.0) val smoothingFactor: Float,
        override val mappingPoints: List<MappingPoint>,
    ) : BrushInputConfig

    /**
     * Configuration for rotation-based brush input.
     *
     * @property smoothingFactor A float value representing the smoothing factor, constrained between 0.0 and 1.0.
     * @property mappingPoints A list of MappingPoint objects that define the input-output mapping for rotation.
     *                         The input represents the rotation angle with the value is constrained between -180.0 and 180.0.
     */
    class Rotation(
        @FloatRange(from = 0.0, to = 1.0) val smoothingFactor: Float,
        override val mappingPoints: List<MappingPoint>,
    ) : BrushInputConfig

    /**
     * Configuration for random-based brush input.
     *
     * @property seed A long value representing the seed for the random number generator.
     * @property mappingPoints A list of MappingPoint objects that define the input-output mapping for random.
     *                        The input represents the random value with the value is constrained between 0.0 and 1.0.
     */
    class Random(
        val seed: Long = 0,
        override val mappingPoints: List<MappingPoint>,
    ) : BrushInputConfig

    /**
     * Configuration for constant-based brush input.
     *
     * @property value A float value representing the constant output value.
     */
    class Constant(
        val value: Float,
        override val mappingPoints: List<MappingPoint> = emptyList(),
    ) : BrushInputConfig
}

/**
 * A data class represents mapping point from an input value to an output value.
 */
data class MappingPoint(
    val input: Float,
    val output: Float,
)
