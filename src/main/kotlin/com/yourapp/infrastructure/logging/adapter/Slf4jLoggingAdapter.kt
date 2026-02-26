package com.yourapp.infrastructure.logging.adapter

import com.yourapp.application.logging.CorrelationIdContext
import com.yourapp.application.logging.LoggingPort
import com.yourapp.domain.logging.ExceptionInfo
import com.yourapp.domain.logging.LogEntry
import com.yourapp.domain.logging.LogLevel
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import java.io.PrintWriter
import java.io.StringWriter

@Component
class Slf4jLoggingAdapter : LoggingPort {
    private val logger = LoggerFactory.getLogger(Slf4jLoggingAdapter::class.java)

    override fun log(entry: LogEntry) {
        try {
            populateMDC(entry)

            when (entry.level) {
                LogLevel.TRACE -> logger.trace(entry.message)
                LogLevel.DEBUG -> logger.debug(entry.message)
                LogLevel.INFO -> logger.info(entry.message)
                LogLevel.WARN -> logger.warn(entry.message)
                LogLevel.ERROR -> {
                    if (entry.exception != null) {
                        logger.error(entry.message, createThrowableFromInfo(entry.exception))
                    } else {
                        logger.error(entry.message)
                    }
                }
            }
        } finally {
            MDC.clear()
        }
    }

    override fun debug(message: String, context: Map<String, Any?>) {
        log(createLogEntry(LogLevel.DEBUG, message, context))
    }

    override fun info(message: String, context: Map<String, Any?>) {
        log(createLogEntry(LogLevel.INFO, message, context))
    }

    override fun warn(message: String, context: Map<String, Any?>) {
        log(createLogEntry(LogLevel.WARN, message, context))
    }

    override fun error(message: String, throwable: Throwable?, context: Map<String, Any?>) {
        val exceptionInfo = throwable?.let {
            ExceptionInfo(
                type = it::class.java.simpleName,
                message = it.message,
                stackTrace = getStackTraceAsString(it)
            )
        }

        log(createLogEntry(LogLevel.ERROR, message, context, exceptionInfo))
    }

    private fun createLogEntry(
        level: LogLevel,
        message: String,
        context: Map<String, Any?>,
        exception: ExceptionInfo? = null
    ): LogEntry {
        return LogEntry(
            level = level,
            message = message,
            correlationId = CorrelationIdContext.get(),
            exception = exception,
            additionalContext = context
        )
    }

    private fun populateMDC(entry: LogEntry) {
        entry.correlationId?.let { MDC.put("correlationId", it) }
        entry.layer?.let { MDC.put("layer", it.name) }
        entry.className?.let { MDC.put("className", it) }
        entry.methodName?.let { MDC.put("methodName", it) }
        entry.executionTimeMs?.let { MDC.put("executionTimeMs", it.toString()) }

        entry.additionalContext.forEach { (key, value) ->
            value?.let { MDC.put(key, it.toString()) }
        }

        entry.exception?.let {
            MDC.put("exceptionType", it.type)
            it.message?.let { msg -> MDC.put("exceptionMessage", msg) }
        }
    }

    private fun getStackTraceAsString(throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        return sw.toString()
    }

    private fun createThrowableFromInfo(info: ExceptionInfo): Throwable {
        return RuntimeException("${info.type}: ${info.message}")
    }
}
