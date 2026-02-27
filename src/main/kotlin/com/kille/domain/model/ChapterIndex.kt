package com.kille.domain.model


@JvmInline
value class ChapterIndex(val value: Int) : Comparable<ChapterIndex> {
    init {
        require(value >= 0) { "ChapterIndex must be >= 0" }
    }

    fun next(): ChapterIndex = ChapterIndex(value + 1)
    fun previous(): ChapterIndex? = if (value > 0) ChapterIndex(value - 1) else null

    override fun toString(): String = value.toString()

    override fun compareTo(other: ChapterIndex): Int {
        return value.compareTo(other.value)
    }
}