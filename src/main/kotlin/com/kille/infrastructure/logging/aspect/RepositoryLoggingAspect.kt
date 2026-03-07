package com.kille.infrastructure.logging.aspect

import com.kille.application.logging.CorrelationIdContext
import com.kille.application.logging.LoggingPort
import com.kille.domain.logging.ExceptionInfo
import com.kille.domain.logging.Layer
import com.kille.domain.logging.LogEntry
import com.kille.domain.logging.LogLevel
import com.kille.infrastructure.logging.config.LoggingProperties
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component

@Aspect
@Component
class RepositoryLoggingAspect(
    private val loggingPort: LoggingPort,
    private val loggingProperties: LoggingProperties
) {

    @Around("execution(* com.kille.infrastructure.persistence.adapter.*.*(..))")
    fun logRepositoryOperation(joinPoint: ProceedingJoinPoint): Any? {
        if (!loggingProperties.repositories.enabled) {
            return joinPoint.proceed()
        }

        val className = joinPoint.signature.declaringTypeName.substringAfterLast('.')
        val methodName = joinPoint.signature.name
        val startTime = System.currentTimeMillis()

        val contextMap = mutableMapOf<String, Any?>()
        contextMap["operationType"] = determineOperationType(methodName)
        contextMap["repositoryClass"] = className

        if (loggingProperties.repositories.includeArgs && joinPoint.args.isNotEmpty()) {
            contextMap["args"] = joinPoint.args.joinToString(", ") { sanitizeArg(it) }
        }

        try {
            val result = joinPoint.proceed()
            val executionTime = System.currentTimeMillis() - startTime

            if (loggingProperties.repositories.includeResult) {
                contextMap["result"] = sanitizeArg(result)
            }

            val level = if (executionTime > loggingProperties.repositories.slowQueryThresholdMs) {
                LogLevel.WARN
            } else {
                LogLevel.DEBUG
            }

            if (executionTime > loggingProperties.repositories.slowQueryThresholdMs) {
                contextMap["slowQuery"] = true
            }

            loggingPort.log(
                LogEntry(
                    level = level,
                    message = "Repository operation: $className.$methodName",
                    correlationId = CorrelationIdContext.get(),
                    layer = Layer.INFRASTRUCTURE,
                    className = className,
                    methodName = methodName,
                    executionTimeMs = executionTime,
                    additionalContext = contextMap
                )
            )

            return result
        } catch (ex: Exception) {
            val executionTime = System.currentTimeMillis() - startTime

            loggingPort.log(
                LogEntry(
                    level = LogLevel.ERROR,
                    message = "Repository operation failed: $className.$methodName",
                    correlationId = CorrelationIdContext.get(),
                    layer = Layer.INFRASTRUCTURE,
                    className = className,
                    methodName = methodName,
                    executionTimeMs = executionTime,
                    exception = ExceptionInfo(
                        type = ex::class.java.simpleName,
                        message = ex.message,
                        stackTrace = ex.stackTraceToString()
                    ),
                    additionalContext = contextMap
                )
            )

            throw ex
        }
    }

    private fun determineOperationType(methodName: String): String {
        return when {
            methodName.startsWith("find") || methodName.startsWith("get") || methodName.startsWith("read") -> "READ"
            methodName.startsWith("save") || methodName.startsWith("create") || methodName.startsWith("insert") -> "WRITE"
            methodName.startsWith("update") || methodName.startsWith("modify") -> "UPDATE"
            methodName.startsWith("delete") || methodName.startsWith("remove") -> "DELETE"
            else -> "UNKNOWN"
        }
    }

    private fun sanitizeArg(arg: Any?): String {
        return when (arg) {
            null -> "null"
            is String -> if (arg.length > 50) "${arg.take(50)}..." else arg
            is Collection<*> -> "[${arg.size} items]"
            is Map<*, *> -> "{${arg.size} entries}"
            else -> arg.toString().take(50)
        }
    }
}
