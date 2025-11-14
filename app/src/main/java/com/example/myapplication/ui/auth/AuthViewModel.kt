package com.example.myapplication.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.User
import com.example.myapplication.data.model.UserType
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.AuthResult
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val error: String? = null,
    val isSignUpMode: Boolean = false
)

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    var uiState by mutableStateOf(AuthUiState())
        private set

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            uiState = uiState.copy(currentUser = user)
        }
    }

    fun signIn(email: String, password: String, onSuccess: (UserType) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            uiState = uiState.copy(error = "Email and password are required")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            
            when (val result = authRepository.signIn(email, password)) {
                is AuthResult.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        currentUser = result.user,
                        error = null
                    )
                    onSuccess(result.user.userType)
                }
                is AuthResult.Error -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun signUp(
        email: String,
        password: String,
        fullName: String,
        userType: UserType,
        onSuccess: (UserType) -> Unit
    ) {
        if (email.isBlank() || password.isBlank() || fullName.isBlank()) {
            uiState = uiState.copy(error = "All fields are required")
            return
        }

        if (password.length < 6) {
            uiState = uiState.copy(error = "Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            
            when (val result = authRepository.signUp(email, password, fullName, userType)) {
                is AuthResult.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        currentUser = result.user,
                        error = null
                    )
                    onSuccess(result.user.userType)
                }
                is AuthResult.Error -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun signOut(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            uiState = uiState.copy(currentUser = null)
            onSuccess()
        }
    }

    fun toggleSignUpMode() {
        uiState = uiState.copy(
            isSignUpMode = !uiState.isSignUpMode,
            error = null
        )
    }

    fun clearError() {
        uiState = uiState.copy(error = null)
    }

    fun setError(message: String) {
        uiState = uiState.copy(error = message)
    }

    fun resetPassword(email: String, onSuccess: () -> Unit) {
        if (email.isBlank()) {
            uiState = uiState.copy(error = "Email is required")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            
            val result = authRepository.resetPassword(email)
            
            if (result.isSuccess) {
                uiState = uiState.copy(isLoading = false)
                onSuccess()
            } else {
                uiState = uiState.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to send reset email"
                )
            }
        }
    }
}
