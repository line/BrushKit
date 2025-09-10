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

import com.linecorp.brushkit.core.model.MappingPoint
import kotlin.math.abs

/**
 * A class that interpolates mapping points to provide an output based on a given input.
 *
 * @param mappings A list of MappingPoint objects that define the input-output mapping.
 */
internal class MappingPointInterpolator(mappings: List<MappingPoint>) {
    private val sortedMappingPoints = mappings.sortedBy { it.input }

    /**
     * Retrieves the output value corresponding to the given input.
     *
     * @param input A float value representing the input for which the output is to be determined.
     * @return A float value representing the output, or `null` if no mapping points are available.
     */
    fun getOutput(input: Float): Float? {
        if (sortedMappingPoints.isEmpty()) {
            return null
        }
        if (sortedMappingPoints.size == 1) {
            return sortedMappingPoints.first().output
        }

        if (input <= sortedMappingPoints.first().input) {
            return sortedMappingPoints.first().output
        }
        if (input >= sortedMappingPoints.last().input) {
            return sortedMappingPoints.last().output
        }

        val left = sortedMappingPoints.last { it.input <= input }
        val right = sortedMappingPoints.first { it.input >= input }

        // Check if left.input and right.input are effectively equal
        if (abs(left.input - right.input) < INPUT_EQUALITY_THRESHOLD) {
            return left.output
        }

        val ratio = (input - left.input) / (right.input - left.input)
        return left.output + (right.output - left.output) * ratio
    }

    companion object {
        /**
         * Threshold for considering two input values as effectively equal
         */
        private const val INPUT_EQUALITY_THRESHOLD = 0.01f
    }
}
