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

package com.linecorp.brushkit.sample

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.linecorp.brushkit.BrushOperationManagerFactory
import com.linecorp.brushkit.BrushView
import com.linecorp.brushkit.core.BrushOperationManager
import com.linecorp.brushkit.impl.DefaultUnitConverter
import com.linecorp.brushkit.sample.ui.editor.EditorScreen
import com.linecorp.brushkit.sample.ui.editor.EditorViewModel
import com.linecorp.brushkit.sample.ui.theme.AppTheme
import com.linecorp.brushkit.sample.util.MediaStoreHelper

class BrushSampleActivity : ComponentActivity() {
    private val viewModel: EditorViewModel by viewModels {
        EditorViewModel.provideFactory(
            unitConverter = DefaultUnitConverter(displayMetrics = resources.displayMetrics),
        )
    }
    private val brushOperationManager: BrushOperationManager by lazy {
        BrushOperationManagerFactory.create(
            context = this,
            brushDataProvider = viewModel.brushDataProvider,
        )
    }
    private var brushView: BrushView? = null
    private val mediaStoreHelper by lazy {
        MediaStoreHelper(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                EditorScreen(
                    viewModel = viewModel,
                    brushOperationManager = brushOperationManager,
                    brushViewProvider = {
                        BrushView(
                            context = this,
                            brushOperationManager = brushOperationManager,
                        ).also {
                            brushView = it
                        }
                    },
                    onSave = ::onSaveBrush,
                )
            }
        }
    }

    private fun onSaveBrush() {
        val currentBrushView = brushView ?: return
        val brushBitmapWithBounds = currentBrushView.exportBrushBitmapWithBounds() ?: return

        val success = mediaStoreHelper.saveBitmapToGallery(brushBitmapWithBounds.bitmap)
        val toastMessage = if (success) {
            "Drawing saved to gallery"
        } else {
            "Failed to save drawing"
        }
        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT)
            .show()
    }
}
