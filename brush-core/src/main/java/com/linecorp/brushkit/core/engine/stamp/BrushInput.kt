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
import com.linecorp.brushkit.core.engine.stamp.AngleUtil.to180DegreeRange
import com.linecorp.brushkit.core.model.BrushInputConfig
import kotlin.random.Random
import kotlin.random.nextInt

/**
 * An interface representing different types of brush inputs.
 */
internal sealed interface BrushInput {
    /**
     * Updates the current state based on the provided speed and angle.
     */
    fun updateCurrentState(
        @FloatRange(from = 0.0, to = 1.0) speed: Float,
        @FloatRange(from = -180.0, to = 180.0) angle: Float,
    )

    /**
     * Retrieves the current output value.
     */
    fun getCurrentOutputValue(): Float?

    /**
     * Representing a speed input for a brush.
     */
    class Speed(
        private val config: BrushInputConfig.Speed,
    ) : BrushInput {
        private var currentSpeed: Float = Float.MIN_VALUE
        private var output: Float? = null
        private val mappingInterpolator = MappingPointInterpolator(config.mappingPoints)

        init {
            require(config.smoothingFactor in 0.0..1.0) { "Smoothing factor must be in range 0.0 to 1.0" }
            config.mappingPoints.forEach {
                require(it.input in 0.0..1.0) { "Speed input must be in range 0.0 to 1.0" }
            }
        }

        override fun updateCurrentState(
            @FloatRange(from = 0.0, to = 1.0) speed: Float,
            @FloatRange(from = -180.0, to = 180.0) angle: Float,
        ) {
            require(speed in 0.0..1.0) { "Speed must be in range 0.0 to 1.0" }
            require(angle in -180.0..180.0) { "Angle must be in range -180 to 180" }
            currentSpeed = if (currentSpeed == Float.MIN_VALUE) {
                speed
            } else {
                val deltaSpeed = speed - currentSpeed
                currentSpeed + deltaSpeed * config.smoothingFactor
            }
            val newOutput = mappingInterpolator.getOutput(currentSpeed)
            if (newOutput != null) {
                output = newOutput
            }
        }

        override fun getCurrentOutputValue(): Float? = output
    }

    /**
     * Representing a rotation input for a brush.
     */
    class Rotation(
        private val config: BrushInputConfig.Rotation,
    ) : BrushInput {
        private var currentAngle: Float = Float.MIN_VALUE
        private var output: Float? = null
        private val mappingInterpolator = MappingPointInterpolator(config.mappingPoints)

        init {
            require(config.smoothingFactor in 0.0..1.0) { "Smoothing factor must be in range 0.0 to 1.0" }
            config.mappingPoints.forEach {
                require(it.input in -180.0..180.0) { "Angle input must be in range -180 to 180" }
            }
        }

        override fun updateCurrentState(
            @FloatRange(from = 0.0, to = 1.0) speed: Float,
            @FloatRange(from = -180.0, to = 180.0) angle: Float,
        ) {
            require(speed in 0.0..1.0) { "Speed must be in range 0.0 to 1.0" }
            require(angle in -180.0..180.0) { "Angle must be in range -180 to 180" }
            currentAngle = if (currentAngle == Float.MIN_VALUE) {
                angle
            } else {
                val deltaAngle = to180DegreeRange(angle - currentAngle)
                to180DegreeRange(currentAngle + deltaAngle * config.smoothingFactor)
            }
            val newOutput = mappingInterpolator.getOutput(currentAngle)
            if (newOutput != null) {
                output = newOutput
            }
        }

        override fun getCurrentOutputValue(): Float? = output
    }

    /**
     * Representing a random input for a brush.
     */
    class Random(
        config: BrushInputConfig.Random,
    ) : BrushInput {
        private var output: Float? = null
        private val random = Random(seed = config.seed)
        private val mappingInterpolator = MappingPointInterpolator(config.mappingPoints)

        init {
            config.mappingPoints.forEach {
                require(it.input in 0.0..1.0) { "Random input must be in range 0.0 to 1.0" }
            }
            updateCurrentState(0f, 0f)
        }

        override fun updateCurrentState(
            @FloatRange(from = 0.0, to = 1.0) speed: Float,
            @FloatRange(from = -180.0, to = 180.0) angle: Float,
        ) {
            // use `random.nextInt(0..100`) as `random.nextFloat()` doesn't include 1f in its range
            val randomValue = random.nextInt(0..100).toFloat() / 100f
            output = mappingInterpolator.getOutput(randomValue)
        }

        override fun getCurrentOutputValue(): Float? = output
    }

    /**
     * Representing a constant input for a brush.
     */
    class Constant(
        private val config: BrushInputConfig.Constant,
    ) : BrushInput {
        override fun updateCurrentState(
            @FloatRange(from = 0.0, to = 1.0) speed: Float,
            @FloatRange(from = -180.0, to = 180.0) angle: Float,
        ) = Unit

        override fun getCurrentOutputValue(): Float = config.value
    }
}
