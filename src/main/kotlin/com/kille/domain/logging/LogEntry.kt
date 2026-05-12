package com.kille.domain.logging

import java.time.Instant

data class LogEntry(
    val timestamp: Instant = Instant.now(),
    val level: LogLevel,
    val message: String,
    val correlationId: String? = null,
    val layer: Layer? = null,
    val className: String? = null,
    val methodName: String? = null,
    val executionTimeMs: Long? = null,
    val exception: ExceptionInfo? = null,
    val additionalContext: Map<String, Any?> = emptyMap()
)

enum class LogLevel {
    TRACE, DEBUG, INFO, WARN, ERROR
}

enum class Layer {
    PRESENTATION, APPLICATION, INFRASTRUCTURE
}

data class ExceptionInfo(
    val type: String,
    val message: String?,
    val stackTrace: String?
)
