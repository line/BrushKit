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

package com.linecorp.brushkit.core

import com.linecorp.brushkit.core.engine.BrushEngine
import com.linecorp.brushkit.core.engine.line.LineBrushEngine
import com.linecorp.brushkit.core.engine.stamp.StampBrushEngine
import com.linecorp.brushkit.core.model.Brush
import com.linecorp.brushkit.core.model.BrushPath
import com.linecorp.brushkit.core.model.BrushPoint
import com.linecorp.brushkit.core.model.BrushSetting
import com.linecorp.brushkit.core.model.HistoryRecord

/**
 * Manages brush operations, including handling touch events, rendering brush strokes, and manage
 * drawing history.
 *
 * @param brushDataProvider The source of brush data.
 * @param brushTipFactory The factory for creating brush tips.
 * @param unitConverter The unit converter for converting between different units.
 * @param initialBrushPaths The initial brush paths.
 */
class BrushOperationManager(
    val brushDataProvider: BrushDataProvider,
    private val brushTipFactory: BrushTipFactory,
    private val unitConverter: UnitConverter,
    private val initialBrushPaths: List<BrushPath>,
) {
    private val historyList: MutableList<HistoryRecord> = mutableListOf()
    private val redoList: MutableList<HistoryRecord> = mutableListOf()
    private val historyListeners = mutableListOf<BrushHistoryListener>()
    private var currentBrush: Brush? = null
    private var currentSetting: BrushSetting? = null
    private var currentBrushEngine: BrushEngine? = null
    private var currentBrushPoints = mutableListOf<BrushPoint>()
    private var drawingBrushRenderer: BrushRenderer? = null
    private var allBrushesRenderer: BrushRenderer? = null

    /**
     * The source of touch events.
     */
    val touchEventProvider: TouchEventProvider = TouchEventProvider()

    /**
     * @return `true` if an undo operation can be performed.
     */
    val canUndo: Boolean
        get() = historyList.isNotEmpty()

    /**
     * @return `true` if a redo operation can be performed.
     */
    val canRedo: Boolean
        get() = redoList.isNotEmpty()

    /**
     * @return `true` if there are any visible brush strokes drawn on the canvas.
     */
    val isAnyVisiblePathDrawn: Boolean
        get() = getBrushPaths().any { it.setting !is BrushSetting.Eraser }

    init {
        this.touchEventProvider.addListener { event ->
            val brush = currentBrush ?: return@addListener
            val setting = currentSetting ?: return@addListener
            when (event.type) {
                TouchEventType.Down -> {
                    currentBrushPoints.clear()
                    currentBrushPoints.add(BrushPoint(event.x, event.y, event.deltaTimeMs))

                    currentBrushEngine =
                        createBrushEngine(brush, setting, shouldOnlyRenderAfterStrokeEnds = false)
                    currentBrushEngine?.onStrokeDown(event.x, event.y, event.deltaTimeMs)
                }

                TouchEventType.Move -> {
                    currentBrushPoints.add(BrushPoint(event.x, event.y, event.deltaTimeMs))
                    currentBrushEngine?.onStrokeMove(event.x, event.y, event.deltaTimeMs)
                }

                TouchEventType.Up -> {
                    currentBrushPoints.add(BrushPoint(event.x, event.y, event.deltaTimeMs))
                    currentBrushEngine?.onStrokeUp(event.x, event.y, event.deltaTimeMs)
                    val brushPath = BrushPath(brush.id, setting, currentBrushPoints.toList())
                    if (isAnyVisiblePathDrawn || brushPath.setting !is BrushSetting.Eraser) {
                        addNewHistoryRecord(HistoryRecord.Add(brushPath))
                    }
                }
            }
        }
    }

    /**
     * Sets the renderers for the current and all brushes.
     *
     * @param drawingBrushRenderer The renderer for the current drawing brush. This allows for performance
     * optimization by rendering only the brush the user is currently interacting with, without
     * redrawing all brush strokes. Once the user finishes drawing, the current brush strokes are
     * merged into the [allBrushesRenderer].
     * @param allBrushesRenderer The renderer for all brushes.
     */
    fun setRenderers(
        drawingBrushRenderer: BrushRenderer,
        allBrushesRenderer: BrushRenderer,
    ) {
        this.drawingBrushRenderer = drawingBrushRenderer
        this.allBrushesRenderer = allBrushesRenderer
    }

    /**
     * Dispatches a touch event to the brush operation manager.
     *
     * @param type The type of touch event.
     * @param x The x-coordinate of the touch event.
     * @param y The y-coordinate of the touch event.
     * @param timeMs The time in milliseconds when the touch event occurred.
     */
    fun dispatchTouchEvent(
        type: TouchEventType,
        x: Float,
        y: Float,
        timeMs: Long,
    ) {
        touchEventProvider.dispatchTouchEvent(type, x, y, timeMs)
    }

    /**
     * Adds a listener for brush history changes.
     */
    fun addHistoryListener(listener: BrushHistoryListener) = historyListeners.add(listener)

    /**
     * Removes a listener for brush history changes.
     */
    fun removeHistoryListener(listener: BrushHistoryListener) = historyListeners.remove(listener)

    private fun notifyHistoryChanged() = historyListeners.forEach { it.onHistoryChanged() }

    /**
     * Sets the current brush and its settings.
     */
    fun setBrush(
        brush: Brush,
        setting: BrushSetting,
    ) {
        currentBrush = brush
        currentSetting = setting
    }

    private fun createBrushEngine(
        brush: Brush,
        setting: BrushSetting,
        shouldOnlyRenderAfterStrokeEnds: Boolean,
    ): BrushEngine {
        val currentAllBrushesRenderer = allBrushesRenderer
            ?: throw IllegalStateException("All brushes renderer is not initialized")
        val currentDrawingBrushRenderer = drawingBrushRenderer
            ?: throw IllegalStateException("Current brush renderer is not initialized")
        return when {
            LineBrushEngine.isBrushSupported(brush) ->
                LineBrushEngine(
                    currentAllBrushesRenderer,
                    currentDrawingBrushRenderer,
                    brush,
                    setting,
                )

            StampBrushEngine.isBrushSupported(brush) && setting is BrushSetting.Colored ->
                StampBrushEngine(
                    brushTipFactory,
                    unitConverter,
                    currentAllBrushesRenderer,
                    currentDrawingBrushRenderer,
                    brush,
                    setting,
                    shouldOnlyRenderAfterStrokeEnds,
                )

            else -> throw IllegalArgumentException("Unsupported brush: ${brush.id}")
        }
    }

    /**
     * Retrieves the current brush paths that made up the current state.
     * It includes all visible brushes as well as the eraser brushes.
     */
    fun getBrushPaths(): List<BrushPath> {
        // Find all [HistoryRecord.Add] items after the last [HistoryRecord.Clear] operation
        val reversedBrushPaths = mutableListOf<BrushPath>()
        var isFirstHistoryRecordIncluded = false
        for (index in historyList.lastIndex downTo 0) {
            when (val historyRecord = historyList[index]) {
                is HistoryRecord.Add -> reversedBrushPaths.add(historyRecord.brushPath)
                is HistoryRecord.Clear -> break
            }
            if (index == 0) {
                isFirstHistoryRecordIncluded = true
            }
        }

        // Need to reverse as the order of [reversedBrushPaths] is from the most recent to the oldest brush
        val brushPaths = reversedBrushPaths.reversed()

        return when {
            historyList.isEmpty() -> initialBrushPaths
            isFirstHistoryRecordIncluded -> initialBrushPaths + brushPaths
            else -> brushPaths
        }
    }

    /**
     * Clears all brush strokes.
     */
    fun clear() {
        if (initialBrushPaths.isEmpty() && historyList.isEmpty()) {
            return
        }
        if (historyList.lastOrNull() is HistoryRecord.Clear) {
            return
        }
        addNewHistoryRecord(HistoryRecord.Clear)
        allBrushesRenderer?.clear()
    }

    /**
     * Undo the last brush operation.
     */
    fun undo() {
        if (historyList.isEmpty()) {
            return
        }
        redoList.add(historyList.removeAt(historyList.lastIndex))
        redraw()
        notifyHistoryChanged()
    }

    /**
     * Redo the last undone brush operation.
     */
    fun redo() {
        if (redoList.isEmpty()) {
            return
        }
        val lastItem = redoList.removeAt(redoList.lastIndex)
        historyList.add(lastItem)
        when (lastItem) {
            is HistoryRecord.Add -> draw(listOf(lastItem.brushPath))
            is HistoryRecord.Clear -> redraw()
        }
        notifyHistoryChanged()
    }

    private fun addNewHistoryRecord(record: HistoryRecord) {
        historyList.add(record)
        redoList.clear()
        notifyHistoryChanged()
    }

    /**
     * Redraws all visible brush paths.
     */
    fun redraw() {
        allBrushesRenderer?.clear()
        val brushPaths = getBrushPaths()
        if (brushPaths.isNotEmpty()) {
            draw(brushPaths)
        }
    }

    private fun draw(brushPaths: List<BrushPath>) {
        brushPaths.forEach { item ->
            val brush = brushDataProvider.getBrush(item.brushId)
                ?: throw IllegalArgumentException("Brush not found: ${item.brushId}")
            val brushEngine =
                createBrushEngine(brush, item.setting, shouldOnlyRenderAfterStrokeEnds = true)
            brushEngine.draw(item)
        }
    }
}

private fun BrushEngine.draw(brushPath: BrushPath) {
    brushPath.points.forEachIndexed { index, point ->
        when (index) {
            0 -> onStrokeDown(point.x, point.y, point.deltaTimeMs)
            brushPath.points.lastIndex -> onStrokeUp(point.x, point.y, point.deltaTimeMs)
            else -> onStrokeMove(point.x, point.y, point.deltaTimeMs)
        }
    }
}
