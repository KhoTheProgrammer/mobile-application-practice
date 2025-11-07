package com.example.myapplication.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChangePasswordViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<ChangePasswordEvent?>(null)
    val events: StateFlow<ChangePasswordEvent?> = _events.asStateFlow()

    fun onCurrentPasswordChange(password: String) {
        _uiState.update { it.copy(currentPassword = password, currentPasswordError = null) }
    }

    fun onNewPasswordChange(password: String) {
        _uiState.update { it.copy(newPassword = password, newPasswordError = null) }
    }

    fun onConfirmPasswordChange(password: String) {
        _uiState.update { it.copy(confirmPassword = password, confirmPasswordError = null) }
    }

    fun onCurrentPasswordVisibilityToggle() {
        _uiState.update { it.copy(isCurrentPasswordVisible = !it.isCurrentPasswordVisible) }
    }

    fun onNewPasswordVisibilityToggle() {
        _uiState.update { it.copy(isNewPasswordVisible = !it.isNewPasswordVisible) }
    }

    fun onConfirmPasswordVisibilityToggle() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun onChangePasswordClick() {
        if (validateForm()) {
            changePassword()
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true
        val state = _uiState.value

        if (state.currentPassword.isBlank()) {
            _uiState.update { it.copy(currentPasswordError = "Current password is required") }
            isValid = false
        }

        if (state.newPassword.isBlank()) {
            _uiState.update { it.copy(newPasswordError = "New password is required") }
            isValid = false
        } else if (state.newPassword.length < 6) {
            _uiState.update { it.copy(newPasswordError = "Password must be at least 6 characters") }
            isValid = false
        } else if (state.newPassword == state.currentPassword) {
            _uiState.update { it.copy(newPasswordError = "New password must be different") }
            isValid = false
        }

        if (state.confirmPassword != state.newPassword) {
            _uiState.update { it.copy(confirmPasswordError = "Passwords do not match") }
            isValid = false
        }

        return isValid
    }

    private fun changePassword() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(1500)
            _uiState.update { it.copy(isLoading = false) }
            _events.value = ChangePasswordEvent.ShowMessage("Password changed successfully")
            delay(500)
            _events.value = ChangePasswordEvent.NavigateBack
        }
    }

    fun onEventHandled() {
        _events.value = null
    }
}

data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isCurrentPasswordVisible: Boolean = false,
    val isNewPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val currentPasswordError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null
)

sealed class ChangePasswordEvent {
    data class ShowMessage(val message: String) : ChangePasswordEvent()
    object NavigateBack : ChangePasswordEvent()
}
