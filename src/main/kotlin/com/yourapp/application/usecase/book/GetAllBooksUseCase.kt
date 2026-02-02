package com.yourapp.application.usecase.book

import com.yourapp.application.dto.response.BookResponse
import com.yourapp.application.usecase.UseCaseNoInput
import com.yourapp.domain.repository.BookRepository
import com.yourapp.presentation.mapper.BookMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetAllBooksUseCase(
    private val repository: BookRepository,
    private val mapper: BookMapper
) : UseCaseNoInput<List<BookResponse>> {

    @Transactional(readOnly = true)
    override fun execute(): List<BookResponse> {
        return repository.findAll()
            .map { mapper.toResponse(it) }
    }
}