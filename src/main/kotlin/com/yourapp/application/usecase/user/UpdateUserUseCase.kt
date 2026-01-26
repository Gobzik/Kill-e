package com.yourapp.application.usecase.user

import com.yourapp.application.dto.request.UpdateUserRequest
import com.yourapp.application.usecase.UseCase
import com.yourapp.domain.exception.DomainException
import com.yourapp.domain.model.User
import com.yourapp.domain.repository.UserRepository
import org.springframework.stereotype.Component

/**
 * Use case for updating existing user
 */
@Component
class UpdateUserUseCase(
    private val userRepository: UserRepository
) : UseCase<UpdateUserRequest, User> {

    override suspend fun execute(input: UpdateUserRequest): User {
        val existingUser = userRepository.findById(input.id)
            ?: throw DomainException("User not found with id: ${input.id}")

        val updatedUser = existingUser.copy(
            username = input.username ?: existingUser.username,
            email = input.email ?: existingUser.email,
            role = input.role ?: existingUser.role
        )

        return userRepository.save(updatedUser)
    }
}
