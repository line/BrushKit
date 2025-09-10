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

package com.linecorp.brushkit.sample.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.linecorp.brushkit.core.model.Brush

@Composable
internal fun EditorBottomToolbar(
    brushItems: List<Brush>,
    selectedBrushId: String?,
    colorItems: List<Color>,
    selectedColor: Color?,
    brushSize: Float,
    brushSizeRange: ClosedFloatingPointRange<Float>,
    onBrushItemClick: (Brush) -> Unit,
    onColorItemClick: (Color) -> Unit,
    onBrushSizeChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        tonalElevation = 10.dp,
    ) {
        Column {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            ) {
                items(brushItems) { brush ->
                    BrushItem(
                        thumbnail = brush.thumbnail,
                        isSelected = selectedBrushId == brush.id,
                        modifier = Modifier.size(48.dp),
                        onClick = {
                            onBrushItemClick(brush)
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.size(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            ) {
                items(colorItems) { color ->
                    ColorItem(
                        color = color,
                        isSelected = selectedColor == color,
                        modifier = Modifier.size(30.dp),
                        onClick = {
                            onColorItemClick(color)
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.size(8.dp))

            Slider(
                value = brushSize,
                valueRange = brushSizeRange,
                onValueChange = onBrushSizeChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )

            Spacer(modifier = Modifier.size(8.dp))
        }
    }
}
