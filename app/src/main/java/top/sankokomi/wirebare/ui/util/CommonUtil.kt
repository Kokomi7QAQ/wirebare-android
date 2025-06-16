package top.sankokomi.wirebare.ui.util

fun <T : Comparable<T>> MutableList<T>.mix(other: List<T>) {
    val origin = this
    if (origin.size <= other.size) {
        origin.merge(other)
    } else {
        origin.drop(other)
    }
}

fun <T : Comparable<T>> MutableList<T>.merge(other: List<T>) {
    val origin = this
    val set = hashSetOf<T>()
    set.addAll(origin)
    set.addAll(other)
    origin.mergeInternal(set.toList().sorted())
}

fun <T : Comparable<T>> MutableList<T>.drop(other: List<T>) {
    val origin = this
    val target = other.toMutableList()
    target.removeAll {
        it !in origin
    }
    origin.dropInternal(target)
}

private fun <T : Comparable<T>> MutableList<T>.mergeInternal(other: List<T>) {
    var i = 0 // 当前列表的指针
    var j = 0 // other 列表的指针

    while (j < other.size) {
        when {
            // 当前列表已遍历完：将 other 剩余元素逐个添加到末尾
            i >= this.size -> {
                this.add(other[j])
                i++ // 移动指针到新添加元素之后
                j++
            }
            // 元素相等：跳过（双指针同时后移）
            this[i] == other[j] -> {
                i++
                j++
            }
            // 当前元素 > other 元素：插入缺失的 other 元素
            this[i] > other[j] -> {
                this.add(i, other[j])
                i++ // 插入后指针后移
                j++
            }
            // 当前元素 < other 元素：移动当前列表指针
            else -> i++
        }
    }
}

private fun <T : Comparable<T>> MutableList<T>.dropInternal(other: List<T>) {
    var i = 0 // 当前列表的指针
    var j = 0 // other 列表的指针
    val iterator = this.listIterator() // 安全删除的迭代器[4,7](@ref)

    while (iterator.hasNext() && j < other.size) {
        val current = iterator.next()
        when {
            // 当前元素 < other 元素 → 删除（不在 other 中）
            current < other[j] -> {
                iterator.remove() // 删除当前元素
            }
            // 当前元素 == other 元素 → 保留（移动双指针）
            current == other[j] -> {
                i++
                j++
            }
            // 当前元素 > other 元素 → 移动 other 指针
            else -> j++
        }
    }

    // 删除 other 遍历完后的剩余尾部元素
    while (iterator.hasNext()) {
        iterator.next()
        iterator.remove()
    }
}