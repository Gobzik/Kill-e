package com.audiobook.application.usecase.example

import com.audiobook.application.dto.request.CreateExampleRequest
import com.audiobook.application.dto.response.ExampleResponse
import com.audiobook.application.usecase.UseCase
import com.audiobook.domain.model.ExampleEntity
import com.audiobook.domain.model.Priority
import com.audiobook.domain.repository.ExampleRepository
import com.audiobook.presentation.mapper.ExampleMapper
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
    override fun execute(input: CreateExampleRequest): ExampleResponse {
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
