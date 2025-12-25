package top.sankokomi.wirebare.ui.knet

import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.common.data.CacheStore
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import kotlin.math.ceil
import kotlin.math.log2
import kotlin.math.pow

class BandwidthCartesianLayerRangeProvider: CartesianLayerRangeProvider {

    companion object {
        private val keyNamespace: CacheStore.KeyNamespace = CacheStore.KeyNamespace()
    }

    override fun getMinY(
        minY: Double,
        maxY: Double,
        extraStore: ExtraStore
    ): Double {
        return 0.0
    }

    override fun getMinX(
        minX: Double,
        maxX: Double,
        extraStore: ExtraStore
    ): Double {
        return 0.0
    }

    override fun getMaxY(
        minY: Double,
        maxY: Double,
        extraStore: ExtraStore
    ): Double {
        return 2.0.pow(ceil(log2(maxY.coerceAtLeast(2048.0))).toInt())
    }

    override fun getMaxX(
        minX: Double,
        maxX: Double,
        extraStore: ExtraStore
    ): Double {
        return super.getMaxX(minX, maxX, extraStore)
    }
}