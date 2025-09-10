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
 * Represent a brush model.
 *
 * @param id The unique identifier of the brush.
 * @param thumbnail The thumbnail of the brush.
 * @param style The style of the brush tip.
 * @param spacingInputConfigs The input configurations for dab spacing, defined as a ratio relative to the original brush tip's width.
 * @param scaleInputConfigs The input configurations for scale ratio with the original size of brush tip.
 * @param opacityInputConfigs The input configurations for opacity.
 */
data class Brush(
    val id: String,
    val thumbnail: BrushIcon,
    val style: BrushStyle,
    // Properties only for stamp brush
    val spacingInputConfigs: List<BrushInputConfig> = emptyList(),
    val scaleInputConfigs: List<BrushInputConfig> = emptyList(),
    val opacityInputConfigs: List<BrushInputConfig> = emptyList(),
    val rotationInputConfigs: List<BrushInputConfig> = emptyList(),
)
