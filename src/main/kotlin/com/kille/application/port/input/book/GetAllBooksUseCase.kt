package com.kille.application.port.input.book

import com.kille.presentation.dto.response.BookResponse
import com.kille.application.port.input.UseCaseNoInput
import com.kille.domain.repository.BookRepository
import com.kille.presentation.mapper.BookMapper
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