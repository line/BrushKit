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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.linecorp.brushkit.core.model.BrushIcon
import com.linecorp.brushkit.sample.ui.theme.WhiteAlpha30

@Composable
internal fun BrushItem(
    thumbnail: BrushIcon,
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(4.dp)
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        WhiteAlpha30
    }
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(WhiteAlpha30)
            .border(2.dp, borderColor, shape)
            .clickable(onClick = onClick, enabled = !isSelected),
        contentAlignment = Alignment.Center,
    ) {
        val thumbnailResId = when (thumbnail) {
            is BrushIcon.Drawable -> thumbnail.resId
        }
        Image(
            painter = painterResource(id = thumbnailResId),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary),
        )
    }
}
