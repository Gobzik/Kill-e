package com.audiobook.domain.model

import java.util.UUID

@JvmInline
value class BookId(val value: UUID) {
    companion object {
        fun fromString(value: String): BookId = BookId(UUID.fromString(value))
    }
}