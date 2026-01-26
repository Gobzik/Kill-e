package com.yourapp.presentation.mapper

import com.yourapp.application.dto.response.UserResponse
import com.yourapp.domain.model.User
import org.springframework.stereotype.Component

/**
 * Mapper for converting between domain models and presentation DTOs
 */
@Component
class UserMapper {

    fun toResponse(user: User): UserResponse {
        return UserResponse(
            id = user.id,
            username = user.username,
            email = user.email,
            role = user.role
        )
    }

    fun toResponseList(users: List<User>): List<UserResponse> {
        return users.map { toResponse(it) }
    }
}
