package com.audiobook.domain.model

import com.yourapp.domain.exception.DomainException
import java.util.UUID

@JvmInline
value class ChapterId(val value: UUID) {
    init {
        require(value.toString().isNotBlank()) { "ChapterId cannot be empty" }
    }

    companion object {
        fun generate(): ChapterId = ChapterId(UUID.randomUUID())

        fun fromString(value: String): ChapterId {
            return try {
                ChapterId(UUID.fromString(value))
            } catch (e: IllegalArgumentException) {
                throw DomainException("Invalid ChapterId format: $value", e)
            }
        }
    }

    override fun toString(): String = value.toString()
}