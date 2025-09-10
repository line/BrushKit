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

package com.linecorp.brushkit.sample.data

import android.graphics.Color
import com.linecorp.brushkit.core.BrushDataProvider
import com.linecorp.brushkit.core.model.Brush
import com.linecorp.brushkit.core.model.BrushIcon
import com.linecorp.brushkit.core.model.BrushInputConfig
import com.linecorp.brushkit.core.model.BrushStyle
import com.linecorp.brushkit.core.model.MappingPoint
import com.linecorp.brushkit.sample.R
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap

/**
 * The repository contains all brush types that can be used in the app.
 */
internal class BrushRepository : BrushDataProvider {
    val allBrushList = listOf(
        Brush(
            id = LocalBrushes.Eraser.id,
            thumbnail = BrushIcon.Drawable(R.drawable.ic_brush_eraser),
            style = BrushStyle.Eraser(
                initialSizeDp = SOLID_LINE_BRUSH_INITIAL_SIZE_DP,
            ),
        ),
        Brush(
            id = LocalBrushes.Pen.id,
            thumbnail = BrushIcon.Drawable(R.drawable.ic_brush_pen),
            style = BrushStyle.SolidCircle(
                initialSizeDp = SOLID_LINE_BRUSH_INITIAL_SIZE_DP,
                initialColor = INITIAL_COLOR,
            ),
        ),
        Brush(
            id = LocalBrushes.Marker.id,
            thumbnail = BrushIcon.Drawable(R.drawable.ic_brush_marker),
            style = BrushStyle.Image(
                initialSizeDp = SOLID_LINE_BRUSH_INITIAL_SIZE_DP,
                initialColor = INITIAL_COLOR,
                imageResId = R.drawable.brush_tip_marker,
            ),
            spacingInputConfigs = listOf(BrushInputConfig.Constant(0.05f)),
        ),
        Brush(
            id = LocalBrushes.Heart.id,
            thumbnail = BrushIcon.Drawable(R.drawable.ic_brush_heart),
            style = BrushStyle.Image(
                initialSizeDp = IMAGE_BRUSH_INITIAL_SIZE_DP,
                initialColor = INITIAL_COLOR,
                imageResId = R.drawable.brush_tip_heart,
            ),
            spacingInputConfigs = HalfBrushSizeSpacingConfig,
            rotationInputConfigs = MatchingDrawingLineRotationConfig,
        ),
        Brush(
            id = LocalBrushes.Star.id,
            thumbnail = BrushIcon.Drawable(R.drawable.ic_brush_star),
            style = BrushStyle.Image(
                initialSizeDp = IMAGE_BRUSH_INITIAL_SIZE_DP,
                initialColor = INITIAL_COLOR,
                imageResId = R.drawable.brush_tip_star,
            ),
            spacingInputConfigs = HalfBrushSizeSpacingConfig,
            rotationInputConfigs = MatchingDrawingLineRotationConfig,
        ),
        Brush(
            id = LocalBrushes.Music.id,
            thumbnail = BrushIcon.Drawable(R.drawable.ic_brush_music),
            style = BrushStyle.Image(
                initialSizeDp = IMAGE_BRUSH_INITIAL_SIZE_DP,
                initialColor = INITIAL_COLOR,
                imageResId = R.drawable.brush_tip_music,
            ),
            spacingInputConfigs = HalfBrushSizeSpacingConfig,
            rotationInputConfigs = MatchingDrawingLineRotationConfig,
        ),
        Brush(
            id = LocalBrushes.Flower.id,
            thumbnail = BrushIcon.Drawable(R.drawable.ic_brush_flower),
            style = BrushStyle.Image(
                initialSizeDp = IMAGE_BRUSH_INITIAL_SIZE_DP,
                initialColor = INITIAL_COLOR,
                imageResId = R.drawable.brush_tip_flower,
            ),
            spacingInputConfigs = HalfBrushSizeSpacingConfig,
            rotationInputConfigs = MatchingDrawingLineRotationConfig,
        ),
    )

    /**
     * A map of all available brush ids and their corresponding [Brush].
     */
    private val allBrushMap: ImmutableMap<String, Brush> =
        allBrushList
            .associateBy { it.id }
            .toImmutableMap()

    override fun getBrush(brushId: String): Brush? = allBrushMap[brushId]

    companion object {
        private const val INITIAL_COLOR = Color.BLACK
        private const val SOLID_LINE_BRUSH_INITIAL_SIZE_DP = 10f
        private const val IMAGE_BRUSH_INITIAL_SIZE_DP = 20f
    }
}

/**
 * The spacing input configuration that matches half of the brush size.
 */
private val HalfBrushSizeSpacingConfig = listOf(
    BrushInputConfig.Constant(1.5f),
)

/**
 * The rotation input configuration that matches the current drawing line rotation.
 */
private val MatchingDrawingLineRotationConfig = listOf(
    BrushInputConfig.Rotation(
        smoothingFactor = 1f,
        mappingPoints = listOf(
            MappingPoint(-180f, -180f),
            MappingPoint(180f, 180f),
        ),
    ),
)
