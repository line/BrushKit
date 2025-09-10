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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.linecorp.brushkit.sample.R

@Composable
internal fun EditorTopToolbar(
    onClear: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        tonalElevation = 10.dp,
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(
                onClick = onClear,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_editor_clear),
                    contentDescription = "Clear",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IconButton(
                    onClick = onUndo,
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_editor_tool_undo),
                        contentDescription = "Undo",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                IconButton(
                    onClick = onRedo,
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_editor_tool_redo),
                        contentDescription = "Redo",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            IconButton(
                onClick = onSave,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_editor_confirm),
                    contentDescription = "Save",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
