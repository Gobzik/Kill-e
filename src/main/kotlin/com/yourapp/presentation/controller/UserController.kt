package com.yourapp.presentation.controller

import com.yourapp.application.dto.request.CreateUserRequest
import com.yourapp.application.dto.request.UpdateUserRequest
import com.yourapp.application.dto.response.ApiResponse
import com.yourapp.application.dto.response.UserResponse
import com.yourapp.presentation.viewmodel.UserViewModel
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST Controller for User operations
 * Handles HTTP requests and delegates to ViewModel
 */
@RestController
@RequestMapping("/api/users")
class UserController(
    private val userViewModel: UserViewModel
) {

    @GetMapping("/{id}")
    suspend fun getUserById(@PathVariable id: Long): ResponseEntity<ApiResponse<UserResponse>> {
        userViewModel.getUser(id)

        return when (val state = userViewModel.userState.value) {
            is UserViewModel.UserState.Success -> {
                ResponseEntity.ok(ApiResponse.success(state.user, "User retrieved successfully"))
            }
            is UserViewModel.UserState.NotFound -> {
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found with id: $id"))
            }
            is UserViewModel.UserState.Error -> {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(state.message))
            }
            is UserViewModel.UserState.Initial -> {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Unexpected state"))
            }
        }
    }

    @PostMapping
    suspend fun createUser(@RequestBody request: CreateUserRequest): ResponseEntity<ApiResponse<UserResponse>> {
        userViewModel.createUser(request)

        return when (val state = userViewModel.userState.value) {
            is UserViewModel.UserState.Success -> {
                ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(state.user, "User created successfully"))
            }
            is UserViewModel.UserState.Error -> {
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(state.message))
            }
            else -> {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Unexpected state"))
            }
        }
    }

    @PutMapping("/{id}")
    suspend fun updateUser(
        @PathVariable id: Long,
        @RequestBody request: UpdateUserRequest
    ): ResponseEntity<ApiResponse<UserResponse>> {
        val updateRequest = request.copy(id = id)
        userViewModel.updateUser(updateRequest)

        return when (val state = userViewModel.userState.value) {
            is UserViewModel.UserState.Success -> {
                ResponseEntity.ok(ApiResponse.success(state.user, "User updated successfully"))
            }
            is UserViewModel.UserState.Error -> {
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(state.message))
            }
            else -> {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Unexpected state"))
            }
        }
    }
}
