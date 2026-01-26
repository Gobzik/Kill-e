package com.yourapp.application.usecase.user

import com.yourapp.application.usecase.UseCase
import com.yourapp.domain.model.User
import com.yourapp.domain.repository.UserRepository
import org.springframework.stereotype.Component

/**
 * Use case for getting user by ID
 */
@Component
class GetUserUseCase(
    private val userRepository: UserRepository
) : UseCase<Long, User?> {

    override suspend fun execute(input: Long): User? {
        return userRepository.findById(input)
    }
}
