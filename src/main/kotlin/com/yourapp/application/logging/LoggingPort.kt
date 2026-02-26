package com.yourapp.application.logging

import com.yourapp.domain.logging.LogEntry

interface LoggingPort {
    fun log(entry: LogEntry)

    fun debug(message: String, context: Map<String, Any?> = emptyMap())

    fun info(message: String, context: Map<String, Any?> = emptyMap())

    fun warn(message: String, context: Map<String, Any?> = emptyMap())

    fun error(message: String, throwable: Throwable? = null, context: Map<String, Any?> = emptyMap())
}
