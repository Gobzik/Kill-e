package com.yourapp.application.usecase

/**
 * Base interface for all use cases
 * Use cases represent application business logic
 */
interface UseCase<in Input, out Output> {
    suspend fun execute(input: Input): Output
}

/**
 * Use case without input parameters
 */
interface UseCaseNoInput<out Output> {
    suspend fun execute(): Output
}
