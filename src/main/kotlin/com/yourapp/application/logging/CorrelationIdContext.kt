package com.yourapp.application.logging

import java.util.UUID

object CorrelationIdContext {
    private val correlationIdHolder = ThreadLocal<String>()

    fun get(): String? = correlationIdHolder.get()

    fun set(correlationId: String) {
        correlationIdHolder.set(correlationId)
    }

    fun generate(): String {
        val id = UUID.randomUUID().toString()
        set(id)
        return id
    }

    fun clear() {
        correlationIdHolder.remove()
    }

    fun getOrGenerate(): String = get() ?: generate()
}
