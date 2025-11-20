package com.example.myapplication.auth.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for Login Screen
 * Handles all business logic, validation, and state management
 */
class LoginViewModel : ViewModel() {

    // UI State - holds all the state for the login screen
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Events - one-time events like navigation or showing snackbar
    private val _events = MutableStateFlow<LoginEvent?>(null)
    val events: StateFlow<LoginEvent?> = _events.asStateFlow()

    /**
     * Called when user types in email field
     */
    fun onEmailChange(email: String) {
        _uiState.update { it.copy(
            email = email,
            emailError = null // Clear error when user types
        )}
    }

    /**
     * Called when user types in password field
     */
    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(
            password = password,
            passwordError = null // Clear error when user types
        )}
    }

    /**
     * Called when user toggles password visibility
     */
    fun onPasswordVisibilityToggle() {
        _uiState.update { it.copy(
            isPasswordVisible = !it.isPasswordVisible
        )}
    }

    /**
     * Called when user clicks login button
     */
    fun onLoginClick() {
        // Validate inputs
        val emailValid = validateEmail()
        val passwordValid = validatePassword()

        if (emailValid && passwordValid) {
            performLogin()
        }
    }

    /**
     * Validate email format
     */
    private fun validateEmail(): Boolean {
        val email = _uiState.value.email
        return when {
            email.isBlank() -> {
                _uiState.update { it.copy(emailError = "Email is required") }
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _uiState.update { it.copy(emailError = "Invalid email format") }
                false
            }
            else -> {
                _uiState.update { it.copy(emailError = null) }
                true
            }
        }
    }

    /**
     * Validate password
     */
    private fun validatePassword(): Boolean {
        val password = _uiState.value.password
        return when {
            password.isBlank() -> {
                _uiState.update { it.copy(passwordError = "Password is required") }
                false
            }
            password.length < 6 -> {
                _uiState.update { it.copy(passwordError = "Password must be at least 6 characters") }
                false
            }
            else -> {
                _uiState.update { it.copy(passwordError = null) }
                true
            }
        }
    }

    /**
     * Perform login - this is where you'd call your repository/API
     */
    private fun performLogin() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Simulate API call
            delay(1500)

            // Demo: Route based on email
            val email = _uiState.value.email
            val event = when {
                email.contains("donor", ignoreCase = true) -> LoginEvent.NavigateToDonor
                email.contains("orphanage", ignoreCase = true) -> LoginEvent.NavigateToOrphanage
                else -> {
                    // Default to donor for demo
                    LoginEvent.ShowMessage("Login successful!")
                    LoginEvent.NavigateToDonor
                }
            }

            _uiState.update { it.copy(isLoading = false) }
            _events.value = event
        }
    }

    /**
     * Clear the event after it's been handled
     */
    fun onEventHandled() {
        _events.value = null
    }

    /**
     * Called when user clicks forgot password
     */
    fun onForgotPasswordClick() {
        _events.value = LoginEvent.NavigateToForgotPassword
    }

    /**
     * Called when user clicks sign up
     */
    fun onSignUpClick() {
        _events.value = LoginEvent.NavigateToSignUp
    }
}

/**
 * UI State - represents the current state of the login screen
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null
)

/**
 * Events - one-time actions that the UI should respond to
 */
sealed class LoginEvent {
    object NavigateToDonor : LoginEvent()
    object NavigateToOrphanage : LoginEvent()
    object NavigateToSignUp : LoginEvent()
    object NavigateToForgotPassword : LoginEvent()
    data class ShowMessage(val message: String) : LoginEvent()
}
