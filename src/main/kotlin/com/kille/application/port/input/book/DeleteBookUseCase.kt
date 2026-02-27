package com.kille.application.port.input.book

import com.kille.application.port.input.UseCase
import com.kille.domain.model.BookId
import com.kille.domain.repository.BookRepository
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
