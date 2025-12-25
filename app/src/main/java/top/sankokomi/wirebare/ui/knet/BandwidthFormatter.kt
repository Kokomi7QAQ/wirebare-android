package top.sankokomi.wirebare.ui.knet

import top.sankokomi.wirebare.ui.util.Global

object BandwidthFormatter {

    data class Value(
        val value: Double,
        val unit: BandwidthUnit
    ) {
        fun string(
            bit: Int = 1,
            showUnit: Boolean = false
        ): String {
            val str = "%.${bit}f".format(value)
            if (!showUnit) {
                return str
            }
            return "$str ${unit.symbol}"
        }
    }

    fun format(bytes: Double): Value {
        return if (bytes <= 4 * BandwidthUnit.KB.step) {
            Value(bytes, BandwidthUnit.B)
        } else if (bytes <= 4 * BandwidthUnit.MB.step) {
            Value(bytes / BandwidthUnit.KB.step, BandwidthUnit.KB)
        } else {
            Value(bytes / BandwidthUnit.MB.step, BandwidthUnit.MB)
        }
    }

}