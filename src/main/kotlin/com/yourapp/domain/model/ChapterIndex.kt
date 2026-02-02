package com.yourapp.domain.model


@JvmInline
value class ChapterIndex(val value: Int) {
    init {
        require(value >= 0) { "ChapterIndex must be >= 0" }
    }

    fun next(): ChapterIndex = ChapterIndex(value + 1)
    fun previous(): ChapterIndex? = if (value > 0) ChapterIndex(value - 1) else null

    override fun toString(): String = value.toString()
}