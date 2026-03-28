package com.kille.application.logging.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class LogExecutionTime(
    val thresholdMs: Long = 1000
)
