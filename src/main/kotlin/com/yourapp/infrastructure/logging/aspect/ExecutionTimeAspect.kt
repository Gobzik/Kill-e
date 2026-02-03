package com.yourapp.infrastructure.logging.aspect

import com.yourapp.application.logging.CorrelationIdContext
import com.yourapp.application.logging.LoggingPort
import com.yourapp.application.logging.annotations.LogExecutionTime
import com.yourapp.domain.logging.Layer
import com.yourapp.domain.logging.LogEntry
import com.yourapp.domain.logging.LogLevel
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.stereotype.Component

@Aspect
@Component
class ExecutionTimeAspect(
    private val loggingPort: LoggingPort
) {

    @Around("@annotation(com.yourapp.application.logging.annotations.LogExecutionTime)")
    fun logExecutionTime(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature as MethodSignature
        val annotation = signature.method.getAnnotation(LogExecutionTime::class.java)
        val threshold = annotation.thresholdMs

        val className = joinPoint.signature.declaringTypeName.substringAfterLast('.')
        val methodName = joinPoint.signature.name
        val startTime = System.currentTimeMillis()

        try {
            val result = joinPoint.proceed()
            val executionTime = System.currentTimeMillis() - startTime

            if (executionTime > threshold) {
                loggingPort.log(
                    LogEntry(
                        level = LogLevel.WARN,
                        message = "Slow execution detected: $className.$methodName exceeded threshold of ${threshold}ms",
                        correlationId = CorrelationIdContext.get(),
                        layer = Layer.APPLICATION,
                        className = className,
                        methodName = methodName,
                        executionTimeMs = executionTime,
                        additionalContext = mapOf(
                            "threshold" to threshold,
                            "exceeded" to (executionTime - threshold)
                        )
                    )
                )
            }

            return result
        } catch (ex: Exception) {
            throw ex
        }
    }
}
