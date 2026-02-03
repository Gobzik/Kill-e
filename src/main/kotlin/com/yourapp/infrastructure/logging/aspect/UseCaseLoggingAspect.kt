package com.yourapp.infrastructure.logging.aspect

import com.yourapp.application.logging.CorrelationIdContext
import com.yourapp.application.logging.LoggingPort
import com.yourapp.domain.logging.ExceptionInfo
import com.yourapp.domain.logging.Layer
import com.yourapp.domain.logging.LogEntry
import com.yourapp.domain.logging.LogLevel
import com.yourapp.infrastructure.logging.config.LoggingProperties
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component

@Aspect
@Component
class UseCaseLoggingAspect(
    private val loggingPort: LoggingPort,
    private val loggingProperties: LoggingProperties
) {

    @Around("execution(* com.yourapp.application.usecase.*.*.execute(..))")
    fun logUseCaseExecution(joinPoint: ProceedingJoinPoint): Any? {
        if (!loggingProperties.useCases.enabled) {
            return joinPoint.proceed()
        }

        val className = joinPoint.signature.declaringTypeName.substringAfterLast('.')
        val methodName = joinPoint.signature.name
        val startTime = System.currentTimeMillis()

        val contextMap = mutableMapOf<String, Any?>()
        contextMap["useCaseType"] = className

        if (loggingProperties.useCases.includeArgs && joinPoint.args.isNotEmpty()) {
            contextMap["args"] = joinPoint.args.joinToString(", ") { sanitizeArg(it) }
        }

        try {
            val result = joinPoint.proceed()
            val executionTime = System.currentTimeMillis() - startTime

            if (loggingProperties.useCases.includeResult) {
                contextMap["result"] = sanitizeArg(result)
            }

            loggingPort.log(
                LogEntry(
                    level = LogLevel.INFO,
                    message = "UseCase execution: $className.$methodName",
                    correlationId = CorrelationIdContext.get(),
                    layer = Layer.APPLICATION,
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
                    message = "UseCase execution failed: $className.$methodName",
                    correlationId = CorrelationIdContext.get(),
                    layer = Layer.APPLICATION,
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

    private fun sanitizeArg(arg: Any?): String {
        return when (arg) {
            null -> "null"
            is String -> if (arg.length > 100) "${arg.take(100)}..." else arg
            is Collection<*> -> "[${arg.size} items]"
            is Map<*, *> -> "{${arg.size} entries}"
            else -> arg.toString().take(100)
        }
    }
}
