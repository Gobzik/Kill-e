package com.kille.domain.model

import com.kille.domain.exception.DomainException
import java.util.UUID

@JvmInline
value class BookId(val value: UUID) {
    init {
        require(value.toString().isNotBlank()) { "BookId не может быть пустым" }
    }

    companion object {
        fun generate(): BookId = BookId(UUID.randomUUID())

        fun fromString(value: String): BookId {
            return try {
                BookId(UUID.fromString(value))
            } catch (e: IllegalArgumentException) {
                throw DomainException("Invalid BookId format: $value", e)
            }
        }
    }
}