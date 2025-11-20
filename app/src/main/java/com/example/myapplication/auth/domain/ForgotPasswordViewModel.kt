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
 * ViewModel for Forgot Password Screen
 */
class ForgotPasswordViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<ForgotPasswordEvent?>(null)
    val events: StateFlow<ForgotPasswordEvent?> = _events.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onSendResetLinkClick() {
        if (validateEmail()) {
            sendResetLink()
        }
    }

    private fun validateEmail(): Boolean {
        val email = _uiState.value.email
        return when {
            email.isBlank() -> {
                _uiState.update { it.copy(emailError = "Email is required") }
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _uiState.update { it.copy(emailError = "Invalid email format") }
                false
            }
            else -> {
                _uiState.update { it.copy(emailError = null) }
                true
            }
        }
    }

    private fun sendResetLink() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Simulate API call
            delay(1500)

            _uiState.update { it.copy(
                isLoading = false,
                emailSent = true
            )}
        }
    }

    fun onResendClick() {
        _uiState.update { it.copy(emailSent = false) }
    }

    fun onBackToLoginClick() {
        _events.value = ForgotPasswordEvent.NavigateToLogin
    }

    fun onEventHandled() {
        _events.value = null
    }
}

data class ForgotPasswordUiState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val emailSent: Boolean = false
)

sealed class ForgotPasswordEvent {
    object NavigateToLogin : ForgotPasswordEvent()
}
