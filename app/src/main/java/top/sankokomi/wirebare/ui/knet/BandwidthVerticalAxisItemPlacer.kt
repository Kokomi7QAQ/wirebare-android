package top.sankokomi.wirebare.ui.knet

import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.common.Position
import com.patrykandpatrick.vico.core.common.data.CacheStore
import kotlin.math.ceil
import kotlin.math.log2
import kotlin.math.pow

class BandwidthVerticalAxisItemPlacer : VerticalAxis.ItemPlacer {

    companion object {
        private val keyNamespace: CacheStore.KeyNamespace = CacheStore.KeyNamespace()
    }

    override fun getShiftTopLines(context: CartesianDrawingContext): Boolean {
        return true
    }

    override fun getLabelValues(
        context: CartesianDrawingContext,
        axisHeight: Float,
        maxLabelHeight: Float,
        position: Axis.Position.Vertical,
    ): List<Double> {
        return getWidthMeasurementLabelValues(context, axisHeight, maxLabelHeight, position)
    }

    override fun getWidthMeasurementLabelValues(
        context: CartesianMeasuringContext,
        axisHeight: Float,
        maxLabelHeight: Float,
        position: Axis.Position.Vertical,
    ): List<Double> {
        val yRange = context.ranges.getYRange(position)
        val maxY = yRange.maxY
        val d = 2.0.pow(ceil(log2(maxY)).toInt()) / 4.0
        return listOf(0.0, d, 2 * d, 3 * d, 4 * d)
    }

    override fun getHeightMeasurementLabelValues(
        context: CartesianMeasuringContext,
        position: Axis.Position.Vertical,
    ): List<Double> {
        val yRange = context.ranges.getYRange(position)
        return listOf(yRange.minY, (yRange.minY + yRange.maxY) / 2, yRange.maxY)
    }

    override fun getTopLayerMargin(
        context: CartesianMeasuringContext,
        verticalLabelPosition: Position.Vertical,
        maxLabelHeight: Float,
        maxLineThickness: Float,
    ): Float {
        return maxLineThickness
    }

    override fun getBottomLayerMargin(
        context: CartesianMeasuringContext,
        verticalLabelPosition: Position.Vertical,
        maxLabelHeight: Float,
        maxLineThickness: Float,
    ): Float {
        return maxLineThickness
    }
}
