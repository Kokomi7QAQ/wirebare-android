package top.sankokomi.wirebare.ui.knet

import androidx.annotation.StringRes
import top.sankokomi.wirebare.ui.R

enum class BandwidthUnit(
    val step: Double,
    val symbol: String,
    @param:StringRes val strRes: Int
) {
    B(1.0, "B", R.string.unit_byte),
    KB(1024.0, "KB", R.string.unit_k_byte),
    MB(1024.0 * 1024.0, "MB", R.string.unit_m_byte)
}