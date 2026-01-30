package com.yourapp.application.usecase.example

import com.yourapp.application.dto.request.CreateExampleRequest
import com.yourapp.application.dto.response.ExampleResponse
import com.yourapp.application.usecase.UseCase
import com.yourapp.domain.model.ExampleEntity
import com.yourapp.domain.model.Priority
import com.yourapp.domain.repository.ExampleRepository
import com.yourapp.presentation.mapper.ExampleMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Use Case: Создание новой ExampleEntity.
 *
 * Application Layer координирует бизнес-логику:
 * 1. Валидация входных данных
 * 2. Создание доменной модели
 * 3. Сохранение через репозиторий
 * 4. Преобразование в DTO
 */
@Service
class CreateExampleUseCase(
    private val repository: ExampleRepository,
    private val mapper: ExampleMapper
) : UseCase<CreateExampleRequest, ExampleResponse> {

    @Transactional
    override suspend fun execute(input: CreateExampleRequest): ExampleResponse {
        // 1. Преобразование DTO -> Domain
        val priority = Priority.valueOf(input.priority)

        // 2. Создание доменной модели через фабричный метод
        val entity = ExampleEntity.create(
            title = input.title,
            description = input.description,
            priority = priority,
            estimatedEffort = input.estimatedEffort
        )

        // 3. Сохранение через репозиторий
        val saved = repository.save(entity)

        // 4. Преобразование Domain -> DTO
        return mapper.toResponse(saved)
    }
}
