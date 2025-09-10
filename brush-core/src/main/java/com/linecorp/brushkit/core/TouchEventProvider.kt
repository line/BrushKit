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

/**
 * A class that dispatches touch events to registered listeners.
 */
class TouchEventProvider {
    private val listeners = mutableSetOf<TouchEventListener>()
    private var lastEventTime: Long = 0L

    /**
     * Adds a listener to the set of registered listeners.
     *
     * @param listener The listener to be added.
     */
    fun addListener(listener: TouchEventListener) = listeners.add(listener)

    /**
     * Removes a listener from the set of registered listeners.
     *
     * @param listener The listener to be removed.
     */
    fun removeListener(listener: TouchEventListener) = listeners.remove(listener)

    /**
     * Adds a touch event and notifies all registered listeners.
     * This method also calculates the time difference (deltaTimeMs) since the last event.
     * For 'Down' events, the delta time is 0, as they mark the beginning of a touch sequence.
     *
     * @param type The type of the touch event (Down, Move, Up).
     * @param x The x-coordinate of the touch event.
     * @param y The y-coordinate of the touch event.
     * @param timeMs The timestamp of the current event in milliseconds.
     */
    fun dispatchTouchEvent(
        type: TouchEventType,
        x: Float,
        y: Float,
        timeMs: Long,
    ) {
        val deltaTimeMs = when (type) {
            TouchEventType.Down -> 0L
            TouchEventType.Move,
            TouchEventType.Up,
            -> timeMs - lastEventTime
        }
        notifyListeners(TouchEvent(type, x, y, deltaTimeMs))
        lastEventTime = timeMs
    }

    private fun notifyListeners(event: TouchEvent) =
        listeners
            .toList()
            .forEach { it.onTouchEvent(event) }
}

/**
 * Represents the type of touch event.
 */
enum class TouchEventType {
    Down,
    Move,
    Up,
}

/**
 * Data class representing a touch event.
 *
 * @property type The type of the touch event.
 * @property x The x-coordinate of the touch event.
 * @property y The y-coordinate of the touch event.
 * @property deltaTimeMs The time difference in milliseconds from the last event.
 */
data class TouchEvent(
    val type: TouchEventType,
    val x: Float,
    val y: Float,
    val deltaTimeMs: Long,
)

/**
 * Functional interface for a listener interested in touch events.
 */
fun interface TouchEventListener {
    fun onTouchEvent(event: TouchEvent)
}
