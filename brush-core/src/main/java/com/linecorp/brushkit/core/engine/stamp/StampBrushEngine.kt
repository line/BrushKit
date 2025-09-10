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

package com.linecorp.brushkit.core.engine.stamp

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.PointF
import android.util.Size
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import com.linecorp.brushkit.core.BrushRenderer
import com.linecorp.brushkit.core.BrushTipFactory
import com.linecorp.brushkit.core.UnitConverter
import com.linecorp.brushkit.core.engine.BrushEngine
import com.linecorp.brushkit.core.engine.stamp.AngleUtil.to180DegreeRange
import com.linecorp.brushkit.core.model.Brush
import com.linecorp.brushkit.core.model.BrushInputConfig
import com.linecorp.brushkit.core.model.BrushSetting
import com.linecorp.brushkit.core.model.BrushStyle
import com.linecorp.brushkit.core.model.DabRenderInfo
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.hypot
import kotlin.math.min

/**
 * A brush engine that renders brush strokes by drawing multiple brush tips between points.
 * Each drawn brush tip is called a "dab" or "stamp".
 *
 * @param brushTipFactory The factory used to create the brush tip bitmap.
 * @param unitConverter The unit converter used to convert between different units.
 * @param allBrushesRenderer The renderer used to render all brushes.
 * @param drawingBrushRenderer The renderer used to render the current brush.
 * @param brush The brush to be used for rendering.
 */
internal class StampBrushEngine(
    brushTipFactory: BrushTipFactory,
    private val unitConverter: UnitConverter,
    private val allBrushesRenderer: BrushRenderer,
    private val drawingBrushRenderer: BrushRenderer,
    private val brush: Brush,
    setting: BrushSetting.Colored,
    private val shouldOnlyRenderAfterStrokeEnds: Boolean,
) : BrushEngine {
    private val scaleInputs: List<BrushInput> = brush.scaleInputConfigs.map { it.toBrushInput() }
    private val opacityInputs: List<BrushInput> =
        brush.opacityInputConfigs.map { it.toBrushInput() }
    private val rotationInputs: List<BrushInput> =
        brush.rotationInputConfigs.map { it.toBrushInput() }
    private val spacingInputs: List<BrushInput> =
        brush.spacingInputConfigs.map { it.toBrushInput() }
    private val allInputs: List<BrushInput> =
        scaleInputs + opacityInputs + rotationInputs + spacingInputs

    @ColorInt
    private val colorFromSetting: Int = setting.color
    private val sizePxFromSetting: Float = setting.sizePx

    private val colorWithoutOpacity: Int = colorFromSetting or 0xFF000000.toInt()
    private val originalDabSizePx: Size
    private val pendingDabRenderingList = mutableListOf<DabRenderInfo>()

    // Brush state properties
    private var brushTip: Bitmap
    private var currentDabSizePx: Size
    private var currentDabSpacingPx: Float = 0f
    private var currentAngle: Float = 0f
    private var currentSpeed: Float = 0f
    private var isRenderingFirstDab = true

    // Properties for calculating the Bezier curve
    private val measuringPath = Path()
    private val pathMeasurer = PathMeasure()
    private val measuredPosition = FloatArray(2)
    private val measuredPoint = PointF()

    private var lastRenderingPoint: PointF = PointF(INVALID_POINT.x, INVALID_POINT.y)
    private val renderPoints = mutableListOf<RenderPoint>()
    private var unprocessedRenderingIndex = 0
    private val minDistanceForRenderFirstDabPx =
        unitConverter.dpToPx(MIN_DISTANCE_FOR_RENDERING_FIRST_DAB_DP)

    init {
        require(isBrushSupported(brush)) {
            "Unsupported brush style: ${brush.style}"
        }

        brushTip = when (setting) {
            is BrushSetting.Colored.Image -> {
                val style = brush.style as? BrushStyle.Image
                    ?: throw IllegalArgumentException("Invalid brush style: $brush.style")
                brushTipFactory.createBitmapTip(
                    resId = style.imageResId,
                    sizePx = ceil(setting.sizePx).toInt(),
                )
            }

            is BrushSetting.Colored.SoftCircle ->
                brushTipFactory.createSoftCircleTip(
                    sizePx = ceil(setting.sizePx).toInt(),
                    hardness = setting.hardness,
                )

            else -> throw IllegalArgumentException("Invalid brush setting: $setting")
        }

        originalDabSizePx = Size(brushTip.width, brushTip.height)
        currentDabSizePx = Size(brushTip.width, brushTip.height)
        drawingBrushRenderer.setDabBitmap(brushTip, colorWithoutOpacity)
    }

    override fun onStrokeDown(
        x: Float,
        y: Float,
        deltaTimeMs: Long,
    ) {
        // Calculate initial dab size and spacing
        calculateDabSizeAndSpacing()

        renderPoints.clear()
        renderPoints.add(RenderPoint(PointF(x, y), deltaTimeMs))
    }

    override fun onStrokeMove(
        x: Float,
        y: Float,
        deltaTimeMs: Long,
    ) {
        renderStrokeTo(x, y, deltaTimeMs, isFinalPoint = false)
    }

    override fun onStrokeUp(
        x: Float,
        y: Float,
        deltaTimeMs: Long,
    ) {
        renderStrokeTo(x, y, deltaTimeMs, isFinalPoint = true)
        renderPendingDabs()
        drawingBrushRenderer.renderCurrentStateTo(allBrushesRenderer, 0f, 0f)
        drawingBrushRenderer.clear()
    }

    private fun renderStrokeTo(
        x: Float,
        y: Float,
        deltaTimeMs: Long,
        isFinalPoint: Boolean,
    ) {
        renderPoints.add(RenderPoint(PointF(x, y), deltaTimeMs))

        // Render the first dab if possible
        val lastPoint = renderPoints.last()
        if (isRenderingFirstDab) {
            val firstPoint = renderPoints.first()
            val totalDistance = firstPoint.value.distanceTo(lastPoint.value)
            val distanceForRenderFirstDab = min(minDistanceForRenderFirstDabPx, currentDabSpacingPx)
            if (totalDistance >= distanceForRenderFirstDab) {
                currentSpeed = calculateSpeedForUnprocessedPoints() ?: 0f
                currentAngle = calculateAngle(firstPoint.value, lastPoint.value) ?: 0f
                renderFirstDab(firstPoint)
            } else if (isFinalPoint) {
                // Keep currentSpeed and currentAngle as zero for the first dab when the user only taps the screen
                renderFirstDab(firstPoint)
                return
            } else {
                return
            }
        }

        // Structure the curve path between lastRenderingPoint, previousPoint and endPoint
        val previousPoint = renderPoints[renderPoints.size - 2]
        val endPoint = if (isFinalPoint) {
            lastPoint.value
        } else {
            // The mid point between the current touch point and the previous touch point
            PointF(
                (lastPoint.value.x + previousPoint.value.x) / 2f,
                (lastPoint.value.y + previousPoint.value.y) / 2f,
            )
        }
        pathMeasurer.setPoints(lastRenderingPoint, previousPoint.value, endPoint)
        val totalDistance = pathMeasurer.length

        // Render dabs along the curve path
        if (currentDabSpacingPx <= totalDistance) {
            currentSpeed = calculateSpeedForUnprocessedPoints() ?: 0f
        }
        var movingDistance = 0f
        while (movingDistance + currentDabSpacingPx <= totalDistance) {
            movingDistance += currentDabSpacingPx
            val dabCoordinate = pathMeasurer.getPointFromDistance(movingDistance)
            currentAngle = calculateAngle(lastRenderingPoint, dabCoordinate) ?: 0f
            requestRenderDab(dabCoordinate)
        }

        if (movingDistance > 0f) {
            unprocessedRenderingIndex = renderPoints.lastIndex
        }

        // Render the pending list if needed
        if (!shouldOnlyRenderAfterStrokeEnds) {
            renderPendingDabs()
        }
    }

    private fun renderFirstDab(point: RenderPoint) {
        requestRenderDab(point.value)
        isRenderingFirstDab = false
    }

    private fun calculateSpeedForUnprocessedPoints(): Float? {
        val unprocessedPoints = renderPoints.subList(unprocessedRenderingIndex, renderPoints.size)
        val totalDeltaTime = unprocessedPoints.sumOf { it.deltaTimeMs }
        val totalDistance = unprocessedPoints
            .zipWithNext { a, b -> a.value.distanceTo(b.value) }
            .sum()
        return calculateSpeed(totalDistance, totalDeltaTime)
    }

    private fun requestRenderDab(point: PointF) {
        // Need to call this for every dab as the output values will be different for each dab
        allInputs.forEach { it.updateCurrentState(currentSpeed, currentAngle) }

        // Update the brush state based on the current value of inputs
        calculateDabSizeAndSpacing()
        val scale = currentDabSizePx.width.toFloat() / originalDabSizePx.width
        val opacityFromInputs = opacityInputs.getCurrentOutputValue(MIN_OPACITY, MAX_OPACITY) ?: 1f
        val opacity = opacityFromInputs * Color.valueOf(colorFromSetting).alpha()
        val rotation = rotationInputs.getCurrentOutputValue(MIN_ROTATION, MAX_ROTATION) ?: 0f

        // Request render the dab by adding it to the pending list
        pendingDabRenderingList.add(DabRenderInfo(point.x, point.y, scale, rotation, opacity))
        lastRenderingPoint.set(point)
    }

    private fun renderPendingDabs() {
        if (pendingDabRenderingList.isEmpty()) {
            return
        }
        drawingBrushRenderer.renderDabList(pendingDabRenderingList)
        pendingDabRenderingList.clear()
    }

    private fun calculateDabSizeAndSpacing() {
        val scale = scaleInputs.getCurrentOutputValue(MIN_SCALE, null) ?: 1f
        currentDabSizePx = currentDabSizePx.scaleToFit(sizePxFromSetting * scale)
        if (currentDabSizePx.width <= 0 || currentDabSizePx.height <= 0) {
            throw IllegalArgumentException("Invalid brush size")
        }
        val spacingToActualSizeRatio =
            spacingInputs.getCurrentOutputValue(MIN_SPACING_TO_SIZE_RATIO, null) ?: 1f
        currentDabSpacingPx = spacingToActualSizeRatio * currentDabSizePx.width
        if (currentDabSpacingPx <= 0f) {
            throw IllegalArgumentException("Invalid spacing")
        }
    }

    /**
     * Calculates the speed value based on the distance and time.
     *
     * @return The speed value between 0.0 and 1.0, or null if the speed cannot be calculated.
     *         The speed value is normalized between 0.0 and 1.0 based on the minimum and maximum speed.
     *         In which 1.0 is the maximum speed, which equals to [MAX_SPEED_MILLIMETER_PER_MS].
     */
    @FloatRange(from = 0.0, to = 1.0)
    private fun calculateSpeed(
        distancePx: Float,
        deltaTimeMs: Long,
    ): Float? {
        if (deltaTimeMs <= 0) {
            return null
        }
        val currentSpeedPxPerMs = distancePx / deltaTimeMs
        val currentSpeedMillimeterPerMs = unitConverter
            .pxToMillimeter(currentSpeedPxPerMs)
            .coerceIn(MIN_SPEED_MILLIMETER_PER_MS, MAX_SPEED_MILLIMETER_PER_MS)
        return currentSpeedMillimeterPerMs / (MAX_SPEED_MILLIMETER_PER_MS - MIN_SPEED_MILLIMETER_PER_MS)
    }

    /**
     * Calculates the angle between two points and the x-axis.
     */
    @FloatRange(from = -180.0, to = 180.0)
    private fun calculateAngle(
        firstPoint: PointF,
        secondPoint: PointF,
    ): Float? {
        if (firstPoint == secondPoint) {
            return null
        }
        val dx = secondPoint.x - firstPoint.x
        val dy = secondPoint.y - firstPoint.y
        val angleInRadian = atan2(dy, dx).toDouble()
        return to180DegreeRange(Math.toDegrees(angleInRadian).toFloat())
    }

    /**
     * Sets the points for the measuring curve path.
     *
     * @param startPoint The start point of the curve path.
     * @param controlPoint The control point of the curve path.
     * @param endPoint The end point of the curve path.
     */
    private fun PathMeasure.setPoints(
        startPoint: PointF,
        controlPoint: PointF,
        endPoint: PointF,
    ) {
        measuringPath.apply {
            reset()
            moveTo(startPoint.x, startPoint.y)
            quadTo(controlPoint.x, controlPoint.y, endPoint.x, endPoint.y)
        }
        setPath(measuringPath, false)
    }

    /**
     * Gets the point on the curve path at the given distance.
     */
    private fun PathMeasure.getPointFromDistance(distance: Float): PointF {
        getPosTan(distance, measuredPosition, null)
        measuredPoint.set(measuredPosition[0], measuredPosition[1])
        return measuredPoint
    }

    companion object {
        private val INVALID_POINT = PointF(Float.MIN_VALUE, Float.MIN_VALUE)
        private const val MIN_SCALE = 0.1f
        private const val MIN_SPACING_TO_SIZE_RATIO = 0.05f
        private const val MIN_OPACITY = 0f
        private const val MAX_OPACITY = 1f
        private const val MIN_ROTATION = -180f
        private const val MAX_ROTATION = 180f
        private const val MIN_SPEED_MILLIMETER_PER_MS = 0f
        private const val MAX_SPEED_MILLIMETER_PER_MS = 1f
        private const val MIN_DISTANCE_FOR_RENDERING_FIRST_DAB_DP = 12f

        private data class RenderPoint(
            val value: PointF,
            val deltaTimeMs: Long,
        )

        /**
         * Checks if the given brush is supported.
         */
        fun isBrushSupported(brush: Brush): Boolean =
            when (brush.style) {
                is BrushStyle.SoftCircle,
                is BrushStyle.Image,
                -> true

                else -> false
            }

        /**
         * Converts a [BrushInputConfig] to a [BrushInput].
         */
        private fun BrushInputConfig.toBrushInput(): BrushInput = when (this) {
            is BrushInputConfig.Constant -> BrushInput.Constant(this)
            is BrushInputConfig.Speed -> BrushInput.Speed(this)
            is BrushInputConfig.Random -> BrushInput.Random(this)
            is BrushInputConfig.Rotation -> BrushInput.Rotation(this)
        }

        /**
         * Calculates the current output value from a list of [BrushInput]s by summing all the outputs.
         */
        private fun List<BrushInput>.getCurrentOutputValue(
            min: Float?,
            max: Float?,
        ): Float? {
            val outputs = this.mapNotNull { it.getCurrentOutputValue() }
            if (outputs.isEmpty()) {
                return null
            }
            return outputs
                .sum()
                .coerceIn(min, max)
        }

        /**
         * Calculates the distance between two points.
         */
        private fun PointF.distanceTo(otherPoint: PointF): Float =
            hypot(x - otherPoint.x, y - otherPoint.y)

        /**
         * Scales the size of the [Size] to fit the given value.
         */
        private fun Size.scaleToFit(target: Float): Size {
            require(target > 0) {
                "value must be greater than 0"
            }
            val widthScale = target / width
            val heightScale = target / height
            val scaleFactor = min(widthScale, heightScale)
            return Size((width * scaleFactor).toInt(), (height * scaleFactor).toInt())
        }
    }
}
