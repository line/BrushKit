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

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.linecorp.brushkit.core.BrushDataProvider
import com.linecorp.brushkit.core.UnitConverter
import com.linecorp.brushkit.core.model.Brush
import com.linecorp.brushkit.core.model.BrushSetting
import com.linecorp.brushkit.core.model.BrushStyle
import com.linecorp.brushkit.sample.data.BrushRepository
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableMap

internal class EditorViewModel(
    private val unitConverter: UnitConverter,
    brushRepository: BrushRepository = BrushRepository(),
) : ViewModel() {
    private val brushItemMap: ImmutableMap<String, Brush> =
        brushRepository.allBrushList
            .associateBy { it.id }
            .toImmutableMap()

    /**
     * A map of [Brush] ids and their settings that have been modified by the user.
     */
    val brushSettingMap: MutableMap<String, BrushSetting> = mutableStateMapOf()

    val brushDataProvider: BrushDataProvider = brushRepository

    /**
     * A list of all available brush items.
     */
    val brushItems: List<Brush> = brushItemMap.values.toList()

    /**
     * The ID of the currently selected brush.
     */
    var selectedBrushId: String? by mutableStateOf(null)

    /**
     * The currently selected brush item.
     */
    val selectedBrush: Brush? by derivedStateOf {
        brushItemMap[selectedBrushId]
    }

    /**
     * The brush setting of the currently selected brush and has been modified by the user.
     */
    val selectedBrushSetting: BrushSetting? by derivedStateOf {
        selectedBrush?.let { brush ->
            brushSettingMap.getOrPut(
                key = brush.id,
                defaultValue = { brush.style.toInitialSetting() },
            )
        }
    }

    val colorList: List<Color> = DefaultColorList

    private fun BrushStyle.toInitialSetting(): BrushSetting =
        when (this) {
            is BrushStyle.Eraser ->
                BrushSetting.Eraser(sizePx = unitConverter.dpToPx(initialSizeDp))

            is BrushStyle.SolidCircle ->
                BrushSetting.Colored.SolidCircle(sizePx = unitConverter.dpToPx(initialSizeDp), color = initialColor)

            is BrushStyle.SoftCircle ->
                BrushSetting.Colored.SoftCircle(sizePx = unitConverter.dpToPx(initialSizeDp), color = initialColor, hardness = initialHardness)

            is BrushStyle.Image ->
                BrushSetting.Colored.Image(sizePx = unitConverter.dpToPx(initialSizeDp), color = initialColor)
        }

    companion object {
        fun provideFactory(
            unitConverter: UnitConverter,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return EditorViewModel(unitConverter) as T
            }
        }

        private val DefaultColorList = persistentListOf(
            Color.White,
            Color.Black,
            Color(0xFFC9162B),
            Color(0xFFF24500),
            Color(0xFFFFE500),
            Color(0xFF37AB27),
            Color(0xFF0279D4),
            Color(0xFF2F59CC),
            Color(0xFFAF36C7),
            Color(0xFFFF70B0),
            Color(0xFFFF697A),
            Color(0xFFFFD86B),
            Color(0xFF72DE54),
            Color(0xFF26D1D1),
            Color(0xFF11CDF2),
            Color(0xFFA17DF5),
        )
    }
}
