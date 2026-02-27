package com.audiobook.application.usecase.example

import com.audiobook.application.dto.response.ExampleResponse
import com.audiobook.application.usecase.QueryUseCase
import com.audiobook.domain.repository.ExampleRepository
import com.audiobook.presentation.mapper.ExampleMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Use Case: Получение всех ExampleEntity.
 */
@Service
class GetAllExamplesUseCase(
    private val repository: ExampleRepository,
    private val mapper: ExampleMapper
) : QueryUseCase<List<ExampleResponse>> {

    @Transactional(readOnly = true)
    override fun execute(): List<ExampleResponse> {
        return repository.findAll()
            .map { mapper.toResponse(it) }
    }
}
