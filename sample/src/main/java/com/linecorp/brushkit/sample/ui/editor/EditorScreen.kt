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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.linecorp.brushkit.BrushOperationManagerFactory
import com.linecorp.brushkit.BrushView
import com.linecorp.brushkit.core.BrushOperationManager
import com.linecorp.brushkit.core.model.BrushSetting
import com.linecorp.brushkit.impl.DefaultUnitConverter
import com.linecorp.brushkit.sample.ui.theme.AppTheme

@Composable
internal fun EditorScreen(
    viewModel: EditorViewModel,
    brushOperationManager: BrushOperationManager,
    brushViewProvider: () -> BrushView,
    onSave: () -> Unit,
) {
    var brushView by remember {
        mutableStateOf<BrushView?>(null)
    }
    Surface {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            EditorTopToolbar(
                onClear = brushOperationManager::clear,
                onUndo = brushOperationManager::undo,
                onRedo = brushOperationManager::redo,
                onSave = onSave,
            )
            HorizontalDivider()
            AndroidView(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                factory = { context ->
                    brushViewProvider
                        .invoke()
                        .also {
                            brushView = it
                        }
                },
            )
            HorizontalDivider()

            val selectedBrushSetting = viewModel.selectedBrushSetting
            val selectedColor = if (selectedBrushSetting is BrushSetting.Colored) {
                Color(selectedBrushSetting.color)
            } else {
                null
            }

            val minSizePx = with(LocalDensity.current) { MinBrushSize.toPx() }
            val maxSizePx = with(LocalDensity.current) { MaxBrushSize.toPx() }
            val brushSizeRange = remember {
                minSizePx..maxSizePx
            }
            EditorBottomToolbar(
                brushItems = viewModel.brushItems,
                selectedBrushId = viewModel.selectedBrushId,
                colorItems = viewModel.colorList,
                selectedColor = selectedColor,
                brushSize = selectedBrushSetting?.sizePx ?: minSizePx,
                brushSizeRange = brushSizeRange,
                onBrushItemClick = { brush ->
                    viewModel.selectedBrushId = brush.id
                    val selectedBrushSetting = viewModel.selectedBrushSetting
                    if (selectedBrushSetting != null) {
                        brushOperationManager.setBrush(brush, selectedBrushSetting)
                    }
                },
                onColorItemClick = { color ->
                    val selectedBrush = viewModel.selectedBrush
                    val selectedBrushSetting = viewModel.selectedBrushSetting
                    if (selectedBrush != null && selectedBrushSetting != null) {
                        if (selectedBrushSetting is BrushSetting.Colored) {
                            val updatedSetting = selectedBrushSetting.copyWithColor(color = color.toArgb())
                            viewModel.brushSettingMap[selectedBrush.id] = updatedSetting
                            brushOperationManager.setBrush(selectedBrush, updatedSetting)
                        }
                    }
                },
                onBrushSizeChange = { size ->
                    val selectedBrush = viewModel.selectedBrush
                    val selectedBrushSetting = viewModel.selectedBrushSetting
                    if (selectedBrush != null && selectedBrushSetting != null) {
                        val updatedSetting = selectedBrushSetting.copyWithSize(sizePx = size)
                        viewModel.brushSettingMap[selectedBrush.id] = updatedSetting
                        brushOperationManager.setBrush(selectedBrush, updatedSetting)
                    }
                },
            )
        }
    }
}

private val MinBrushSize = 5.dp
private val MaxBrushSize = 50.dp

@Preview
@Composable
private fun EditorScreenPreview() {
    val context = LocalContext.current
    val viewModel = remember {
        EditorViewModel(
            unitConverter = DefaultUnitConverter(context.resources.displayMetrics),
        )
    }
    val brushViewProvider = remember {
        {
            BrushView(
                context = context,
                brushOperationManager = BrushOperationManagerFactory.create(
                    context = context,
                    brushDataProvider = viewModel.brushDataProvider,
                ),
            )
        }
    }
    val brushOperationManager = remember {
        BrushOperationManagerFactory.create(
            context = context,
            brushDataProvider = viewModel.brushDataProvider,
        )
    }
    AppTheme {
        EditorScreen(
            viewModel = viewModel,
            brushOperationManager = brushOperationManager,
            brushViewProvider = brushViewProvider,
            onSave = { },
        )
    }
}
