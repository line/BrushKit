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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_POINTER_DOWN
import android.view.MotionEvent.ACTION_POINTER_UP
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.widget.FrameLayout
import com.linecorp.brushkit.core.BrushOperationManager
import com.linecorp.brushkit.core.TouchEventType
import com.linecorp.brushkit.extension.getBoundingRect
import com.linecorp.brushkit.impl.DefaultBrushDataProvider

/**
 * A view that allows the user to draw on it using a brush.
 * It integrates a [BrushOperationManager] that handles different type of brush strokes and their history.
 *
 * @param context The context in which the view is running.
 * @param attrs The attribute set containing the view's attributes.
 * @property brushOperationManager The [BrushOperationManager] that manages the brush strokes and their history.
 */
class BrushView(
    context: Context,
    attrs: AttributeSet? = null,
    private val brushOperationManager: BrushOperationManager,
) : FrameLayout(context, attrs) {
    private val drawingBrushRenderView = BrushRenderView(context)
    private val allBrushesRenderView = BrushRenderView(context)
    private var trackingPointerId = INVALID_POINTER_ID

    constructor(context: Context) : this(
        context = context,
        brushOperationManager = BrushOperationManagerFactory.create(
            context = context,
            brushDataProvider = DefaultBrushDataProvider(),
        ),
    )

    init {
        // Using 2 render views to reduce bitmap draw calls as we only need to render the current brush bitmap
        // when the user is drawing without rendering the bitmap contains previous brush strokes
        addView(
            allBrushesRenderView,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT),
        )
        addView(
            drawingBrushRenderView,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT),
        )
        brushOperationManager.setRenderers(
            drawingBrushRenderer = drawingBrushRenderView.renderer,
            allBrushesRenderer = allBrushesRenderView.renderer,
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        if (trackingPointerId == INVALID_POINTER_ID && (action == ACTION_DOWN || action == ACTION_POINTER_DOWN)) {
            trackingPointerId = event.getPointerId(event.actionIndex)
        }

        // Avoid processing multiple pointer inputs by handling events only from the tracking pointer
        val eventPointerId = event.getPointerId(event.actionIndex)
        if (eventPointerId != trackingPointerId) {
            return false
        }

        val trackingPointerIndex = event.findPointerIndex(trackingPointerId)
        if (trackingPointerIndex < 0) {
            return false
        }

        val touchX = event.getX(trackingPointerIndex)
        val touchY = event.getY(trackingPointerIndex)
        when (action) {
            ACTION_DOWN, ACTION_POINTER_DOWN -> brushOperationManager.dispatchTouchEvent(
                TouchEventType.Down,
                touchX,
                touchY,
                event.eventTime,
            )

            ACTION_MOVE -> brushOperationManager.dispatchTouchEvent(
                TouchEventType.Move,
                touchX,
                touchY,
                event.eventTime,
            )

            ACTION_UP, ACTION_POINTER_UP -> {
                brushOperationManager.dispatchTouchEvent(
                    TouchEventType.Up,
                    touchX,
                    touchY,
                    event.eventTime,
                )
                trackingPointerId = INVALID_POINTER_ID
            }

            else -> {
                return false
            }
        }
        return true
    }

    /**
     * Exports the current brush strokes into a bitmap along with its bounding rectangle.
     *
     * @return A [BrushBitmapWithBounds] containing the rendered bitmap and its bounding rectangle,
     *         or null if there are no valid brush strokes to render.
     */
    fun exportBrushBitmapWithBounds(): BrushBitmapWithBounds? {
        val brushDataProvider = brushOperationManager.brushDataProvider
        val boundingRect = brushOperationManager
            .getBrushPaths()
            .getBoundingRect(brushDataProvider)
        if (boundingRect == null || boundingRect.isEmpty) {
            return null
        }

        val viewRect = Rect(0, 0, width, height)

        // Intersect the boundingRect with viewRect to get the actual bitmap rect
        val bitmapRect = Rect()
        if (bitmapRect.setIntersect(viewRect, boundingRect).not()) {
            return null
        }

        val bitmap = createBitmap(bitmapRect.width(), bitmapRect.height(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.translate(-bitmapRect.left.toFloat(), -bitmapRect.top.toFloat())
        draw(canvas)
        return BrushBitmapWithBounds(bitmap, bitmapRect)
    }

    companion object {
        private const val INVALID_POINTER_ID = -1
    }
}

private class BrushRenderView(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private var renderBitmap: Bitmap? = null
    val renderer: CanvasRenderer = CanvasRenderer(
        onRendered = ::invalidate,
    )

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int,
    ) {
        super.onSizeChanged(w, h, oldw, oldh)
        createBitmap(w, h, Bitmap.Config.ARGB_8888).also {
            renderBitmap?.recycle()
            renderBitmap = it
            renderer.renderBitmap = it
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        renderBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
    }
}
