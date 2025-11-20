package com.example.myapplication.auth.domain

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.auth.data.AuthRepository
import com.example.myapplication.auth.data.UserType
import com.example.myapplication.auth.data.AuthResult
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSignUpMode: Boolean = false,
    val error: String? = null,
    val currentUserId: String? = null
)

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    var uiState by mutableStateOf(AuthUiState())
        private set

    var isLoggingOut by mutableStateOf(false)
        private set

    init {
        // Load current user on initialization
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()
                uiState = uiState.copy(currentUserId = user?.id)
            } catch (e: Exception) {
                // Silently fail - user not logged in
            }
        }
    }

    fun getCurrentUserId(): String? = uiState.currentUserId

    fun toggleSignUpMode() {
        uiState = uiState.copy(isSignUpMode = !uiState.isSignUpMode, error = null)
    }

    fun clearError() {
        uiState = uiState.copy(error = null)
    }

    fun setError(message: String) {
        uiState = uiState.copy(error = message)
    }

    fun signUp(
        email: String,
        password: String,
        fullName: String,
        userType: UserType,
        onSuccess: (UserType) -> Unit
    ) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                when (val result = authRepository.signUp(email, password, fullName, userType)) {
                    is AuthResult.Success -> {
                        uiState = uiState.copy(
                            isLoading = false,
                            currentUserId = result.user.id
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
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Sign up failed"
                )
            }
        }
    }

    fun signIn(email: String, password: String, onSuccess: (UserType) -> Unit) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                Log.d("AuthViewModel", "Attempting sign in for: $email")
                when (val result = authRepository.signIn(email, password)) {
                    is AuthResult.Success -> {
                        Log.d("AuthViewModel", "Sign in successful, user ID: ${result.user.id}, type: ${result.user.userType}")
                        uiState = uiState.copy(
                            isLoading = false,
                            currentUserId = result.user.id
                        )
                        onSuccess(result.user.userType)
                    }
                    is AuthResult.Error -> {
                        Log.e("AuthViewModel", "Sign in failed: ${result.message}")
                        uiState = uiState.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Sign in exception: ${e.message}", e)
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Sign in failed"
                )
            }
        }
    }

    fun logout(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            isLoggingOut = true
            try {
                val result = authRepository.signOut()
                if (result.isSuccess) {
                    uiState = uiState.copy(currentUserId = null)
                    onSuccess()
                } else {
                    onError(result.exceptionOrNull()?.message ?: "Logout failed")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Logout failed")
            } finally {
                isLoggingOut = false
            }
        }
    }
}
