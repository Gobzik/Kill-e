package com.kille.application.logging

import com.kille.domain.logging.LogEntry

interface LoggingPort {
    fun log(entry: LogEntry)

    fun debug(message: String, context: Map<String, Any?> = emptyMap())

    fun info(message: String, context: Map<String, Any?> = emptyMap())

    fun warn(message: String, context: Map<String, Any?> = emptyMap())

    fun error(message: String, throwable: Throwable? = null, context: Map<String, Any?> = emptyMap())
}
