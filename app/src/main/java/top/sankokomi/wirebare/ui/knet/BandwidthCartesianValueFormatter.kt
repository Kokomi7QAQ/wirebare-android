package top.sankokomi.wirebare.ui.knet

import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.common.data.CacheStore
import top.sankokomi.wirebare.ui.util.Global

class BandwidthCartesianValueFormatter : CartesianValueFormatter {

    companion object {
        private val keyNamespace: CacheStore.KeyNamespace = CacheStore.KeyNamespace()
    }

    override fun format(
        context: CartesianMeasuringContext,
        value: Double,
        verticalAxisPosition: Axis.Position.Vertical?
    ): CharSequence {
        val rangeY = context.ranges.getYRange(verticalAxisPosition)
        val maxY = rangeY.maxY.coerceAtLeast(2048.0)
        val bandwidthValue = context.cacheStore.getOrSet(
            keyNamespace,
            maxY
        ) {
            BandwidthFormatter.format(maxY)
        }
        if (value > 0.0) {
            return BandwidthFormatter.Value(
                value / bandwidthValue.unit.step,
                bandwidthValue.unit
            ).string(0)
        }
        return bandwidthValue.unit.symbol
    }
}