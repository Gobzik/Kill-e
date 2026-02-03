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
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Aspect
@Component
class ControllerLoggingAspect(
    private val loggingPort: LoggingPort,
    private val loggingProperties: LoggingProperties
) {

    @Around("execution(* com.yourapp.presentation.controller.*.*(..))")
    fun logControllerExecution(joinPoint: ProceedingJoinPoint): Any? {
        if (!loggingProperties.controllers.enabled) {
            return joinPoint.proceed()
        }

        val className = joinPoint.signature.declaringTypeName.substringAfterLast('.')
        val methodName = joinPoint.signature.name
        val startTime = System.currentTimeMillis()

        val requestAttributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
        val request = requestAttributes?.request

        val contextMap = mutableMapOf<String, Any?>()
        request?.let {
            contextMap["httpMethod"] = it.method
            contextMap["uri"] = it.requestURI
            contextMap["queryString"] = it.queryString

            if (loggingProperties.controllers.includeHeaders) {
                val headers = it.headerNames.toList()
                    .associateWith { headerName -> it.getHeader(headerName) }
                    .filterKeys { key -> !key.equals("authorization", ignoreCase = true) }
                contextMap["headers"] = headers
            }
        }

        try {
            val result = joinPoint.proceed()
            val executionTime = System.currentTimeMillis() - startTime

            val statusCode = when (result) {
                is ResponseEntity<*> -> result.statusCode.value()
                else -> 200
            }

            contextMap["statusCode"] = statusCode

            val level = when (statusCode) {
                in 200..299 -> LogLevel.INFO
                in 400..499 -> LogLevel.WARN
                else -> LogLevel.ERROR
            }

            loggingPort.log(
                LogEntry(
                    level = level,
                    message = "Controller execution: $className.$methodName",
                    correlationId = CorrelationIdContext.get(),
                    layer = Layer.PRESENTATION,
                    className = className,
                    methodName = methodName,
                    executionTimeMs = executionTime,
                    additionalContext = contextMap
                )
            )

            return result
        } catch (ex: Exception) {
            val executionTime = System.currentTimeMillis() - startTime

            contextMap["statusCode"] = 500

            loggingPort.log(
                LogEntry(
                    level = LogLevel.ERROR,
                    message = "Controller execution failed: $className.$methodName",
                    correlationId = CorrelationIdContext.get(),
                    layer = Layer.PRESENTATION,
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
}
