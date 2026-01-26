package com.yourapp.presentation.viewmodel

import com.yourapp.application.dto.request.CreateUserRequest
import com.yourapp.application.dto.request.UpdateUserRequest
import com.yourapp.application.dto.response.UserResponse
import com.yourapp.application.usecase.user.CreateUserUseCase
import com.yourapp.application.usecase.user.GetUserUseCase
import com.yourapp.application.usecase.user.UpdateUserUseCase
import com.yourapp.presentation.mapper.UserMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.springframework.stereotype.Component

/**
 * ViewModel for User operations
 * Manages UI state and coordinates between use cases and presentation layer
 */
@Component
class UserViewModel(
    private val getUserUseCase: GetUserUseCase,
    private val createUserUseCase: CreateUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val userMapper: UserMapper
) {

    // UI State
    private val _userState = MutableStateFlow<UserState>(UserState.Initial)
    val userState: StateFlow<UserState> = _userState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    suspend fun getUser(id: Long) {
        try {
            _isLoading.value = true
            _error.value = null

            val user = getUserUseCase.execute(id)
            _userState.value = if (user != null) {
                UserState.Success(userMapper.toResponse(user))
            } else {
                UserState.NotFound
            }
        } catch (e: Exception) {
            _error.value = e.message
            _userState.value = UserState.Error(e.message ?: "Unknown error")
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun createUser(request: CreateUserRequest) {
        try {
            _isLoading.value = true
            _error.value = null

            val user = createUserUseCase.execute(request)
            _userState.value = UserState.Success(userMapper.toResponse(user))
        } catch (e: Exception) {
            _error.value = e.message
            _userState.value = UserState.Error(e.message ?: "Unknown error")
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun updateUser(request: UpdateUserRequest) {
        try {
            _isLoading.value = true
            _error.value = null

            val user = updateUserUseCase.execute(request)
            _userState.value = UserState.Success(userMapper.toResponse(user))
        } catch (e: Exception) {
            _error.value = e.message
            _userState.value = UserState.Error(e.message ?: "Unknown error")
        } finally {
            _isLoading.value = false
        }
    }

    // Sealed class for UI states
    sealed class UserState {
        data object Initial : UserState()
        data class Success(val user: UserResponse) : UserState()
        data object NotFound : UserState()
        data class Error(val message: String) : UserState()
    }
}
