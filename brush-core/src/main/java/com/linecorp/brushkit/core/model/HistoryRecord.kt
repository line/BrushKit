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
 * Represents a history record in the history of brush paths.
 */
internal sealed interface HistoryRecord {
    /**
     * A history record representing adding a brush path action
     */
    data class Add(
        val brushPath: BrushPath,
    ) : HistoryRecord

    /**
     * A history record representing a clear action
     */
    object Clear : HistoryRecord
}
