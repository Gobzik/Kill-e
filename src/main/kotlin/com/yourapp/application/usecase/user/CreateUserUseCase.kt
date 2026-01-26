package com.yourapp.application.usecase.user

import com.yourapp.application.dto.request.CreateUserRequest
import com.yourapp.application.usecase.UseCase
import com.yourapp.domain.model.User
import com.yourapp.domain.repository.UserRepository
import org.springframework.stereotype.Component

/**
 * Use case for creating a new user
 */
@Component
class CreateUserUseCase(
    private val userRepository: UserRepository
) : UseCase<CreateUserRequest, User> {

    override suspend fun execute(input: CreateUserRequest): User {
        val user = User(
            id = 0L, // Will be assigned by database
            username = input.username,
            email = input.email,
            role = input.role
        )
        return userRepository.save(user)
    }
}
