package com.yourapp.application.usecase.book

import com.yourapp.application.usecase.UseCase
import com.yourapp.domain.model.BookId
import com.yourapp.domain.repository.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class DeleteBookUseCase(
    private val repository: BookRepository
) : UseCase<UUID, Unit> {

    @Transactional
    override fun execute(input: UUID) {
        repository.delete(BookId(input))
    }
}
