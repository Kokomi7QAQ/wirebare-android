package top.sankokomi.wirebare.ui.util

import kotlin.math.pow

class Damping(
    val coefficient: Float,
    val max: Long
) {

    fun toPercent(value: Long): Float {
        return (value.coerceIn(0, max).toFloat() / max).pow(1.0f / coefficient)
    }

    fun toValue(percent: Float): Long {
        return (max * percent.coerceIn(0.0f, 1.0f).pow(coefficient)).toLong()
    }
}