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

package com.linecorp.brushkit

import android.content.Context
import com.linecorp.brushkit.core.BrushDataProvider
import com.linecorp.brushkit.core.BrushOperationManager
import com.linecorp.brushkit.core.BrushTipFactory
import com.linecorp.brushkit.core.UnitConverter
import com.linecorp.brushkit.core.model.BrushPath
import com.linecorp.brushkit.impl.DefaultBrushTipFactory
import com.linecorp.brushkit.impl.DefaultUnitConverter

/**
 * Factory for creating instances of [BrushOperationManager].
 */
object BrushOperationManagerFactory {
    /**
     * Creates a new instance of [BrushOperationManager].
     *
     * @param context The context to use for creating the brush operation manager.
     * @param brushDataProvider The brush data provider to use for retrieving brush data.
     * @param brushTipFactory The factory to use for creating brush tips. Defaults to [DefaultBrushTipFactory].
     * @param unitConverter The unit converter to use for converting units. Defaults to [DefaultUnitConverter].
     * @param initialBrushPaths The initial brush paths to use. Defaults to an empty list.
     * @return A new instance of [BrushOperationManager].
     */
    fun create(
        context: Context,
        brushDataProvider: BrushDataProvider,
        brushTipFactory: BrushTipFactory = DefaultBrushTipFactory(context),
        unitConverter: UnitConverter = DefaultUnitConverter(context.resources.displayMetrics),
        initialBrushPaths: List<BrushPath> = emptyList(),
    ): BrushOperationManager =
        BrushOperationManager(brushDataProvider, brushTipFactory, unitConverter, initialBrushPaths)
}
