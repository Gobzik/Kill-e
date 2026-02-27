package com.audiobook.application.usecase

interface QueryUseCase<OUT> {
    fun execute(): OUT
}

/**
 * Базовый интерфейс для Query Use Cases с параметрами
 */
interface QueryUseCaseWithParam<IN, OUT> {
    fun execute(param: IN): OUT
}

/**
 * Базовый интерфейс для Command Use Cases (операции записи)
 */
interface CommandUseCase<IN, OUT> {
    fun execute(command: IN): OUT
}

/**
 * Базовый интерфейс для Command Use Cases без результата
 */
interface CommandUseCaseNoResult<IN> {
    fun execute(command: IN)
}