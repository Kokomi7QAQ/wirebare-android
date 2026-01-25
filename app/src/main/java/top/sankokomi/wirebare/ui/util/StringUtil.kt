package top.sankokomi.wirebare.ui.util

fun String?.selfOrNone(): String {
    return this ?: none()
}

fun none(): String = "NONE"
