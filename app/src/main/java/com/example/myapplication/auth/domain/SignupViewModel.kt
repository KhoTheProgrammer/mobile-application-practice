package com.example.myapplication.auth.domain

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for Signup Screen
 */
class SignupViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<SignupEvent?>(null)
    val events: StateFlow<SignupEvent?> = _events.asStateFlow()

    fun onFullNameChange(name: String) {
        _uiState.update { it.copy(fullName = name, nameError = null) }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, confirmPasswordError = null) }
    }

    fun onUserTypeChange(userType: String) {
        _uiState.update { it.copy(userType = userType) }
    }

    fun onPasswordVisibilityToggle() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onConfirmPasswordVisibilityToggle() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun onUserTypeDropdownToggle() {
        _uiState.update { it.copy(isUserTypeDropdownExpanded = !it.isUserTypeDropdownExpanded) }
    }

    fun onSignUpClick() {
        if (validateForm()) {
            performSignup()
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true
        val state = _uiState.value

        // Validate name
        if (state.fullName.isBlank()) {
            _uiState.update { it.copy(nameError = "Name is required") }
            isValid = false
        }

        // Validate email
        if (state.email.isBlank()) {
            _uiState.update { it.copy(emailError = "Email is required") }
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _uiState.update { it.copy(emailError = "Invalid email format") }
            isValid = false
        }

        // Validate password
        if (state.password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Password is required") }
            isValid = false
        } else if (state.password.length < 6) {
            _uiState.update { it.copy(passwordError = "Password must be at least 6 characters") }
            isValid = false
        }

        // Validate confirm password
        if (state.confirmPassword != state.password) {
            _uiState.update { it.copy(confirmPasswordError = "Passwords do not match") }
            isValid = false
        }

        return isValid
    }

    private fun performSignup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Simulate API call
            delay(1500)

            _uiState.update { it.copy(isLoading = false) }
            _events.value = SignupEvent.ShowMessage("Account created successfully!")
            
            // Navigate after a short delay
            delay(500)
            _events.value = SignupEvent.NavigateToHome
        }
    }

    fun onLoginClick() {
        _events.value = SignupEvent.NavigateToLogin
    }

    fun onEventHandled() {
        _events.value = null
    }
}

data class SignupUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val userType: String = "Donor",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isUserTypeDropdownExpanded: Boolean = false,
    val isLoading: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)

sealed class SignupEvent {
    object NavigateToHome : SignupEvent()
    object NavigateToLogin : SignupEvent()
    data class ShowMessage(val message: String) : SignupEvent()
}
