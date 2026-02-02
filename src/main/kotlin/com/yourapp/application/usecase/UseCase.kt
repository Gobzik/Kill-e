package com.yourapp.application.usecase

interface UseCase<in Input, out Output> {
    fun execute(input: Input): Output
}

interface UseCaseNoInput<out Output> {
    fun execute(): Output
}
