package com.yourapp.application.usecase.example

import com.yourapp.application.dto.response.ExampleResponse
import com.yourapp.application.usecase.QueryUseCase
import com.yourapp.domain.repository.ExampleRepository
import com.yourapp.presentation.mapper.ExampleMapper
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
