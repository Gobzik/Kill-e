package com.yourapp.application.logging.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class LogExecutionTime(
    val thresholdMs: Long = 1000
)
