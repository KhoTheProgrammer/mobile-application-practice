package com.example.myapplication.profile.domain

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.profile.data.Address
import com.example.myapplication.profile.data.UserProfile
import com.example.myapplication.auth.data.UserType
import com.example.myapplication.auth.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for Profile Screen
 * Handles profile viewing and editing for both Donor and Orphanage users
 */
class ProfileViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<ProfileEvent?>(null)
    val events: StateFlow<ProfileEvent?> = _events.asStateFlow()

    init {
        loadUserProfile()
    }

    /**
     * Load user profile from Supabase
     */
    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val user = authRepository.getCurrentUser()
                
                if (user != null) {
                    // Create UserProfile from User data
                    val profile = UserProfile(
                        id = user.id,
                        email = user.email,
                        userType = user.userType,
                        fullName = user.fullName,
                        phoneNumber = user.phone ?: "",
                        address = Address(
                            street = "",
                            city = "",
                            state = "",
                            zipCode = "",
                            country = ""
                        )
                    )
                    
                    _uiState.update { it.copy(
                        isLoading = false,
                        userProfile = profile,
                        fullName = profile.fullName,
                        email = profile.email,
                        phoneNumber = profile.phoneNumber,
                        street = profile.address.street,
                        city = profile.address.city,
                        state = profile.address.state,
                        zipCode = profile.address.zipCode,
                        country = profile.address.country
                    )}
                } else {
                    _uiState.update { it.copy(
                        isLoading = false
                    )}
                    _events.value = ProfileEvent.ShowMessage("Failed to load profile")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _events.value = ProfileEvent.ShowMessage("Error loading profile: ${e.message}")
            }
        }
    }

    fun onEditModeToggle() {
        _uiState.update { it.copy(isEditMode = !it.isEditMode) }
    }

    fun onFullNameChange(name: String) {
        _uiState.update { it.copy(fullName = name, fullNameError = null) }
    }

    // Email should not be editable - this function is kept for compatibility but doesn't update
    fun onEmailChange(email: String) {
        // Email cannot be changed - do nothing
    }

    fun onPhoneNumberChange(phone: String) {
        _uiState.update { it.copy(phoneNumber = phone, phoneError = null) }
    }

    fun onStreetChange(street: String) {
        _uiState.update { it.copy(street = street) }
    }

    fun onCityChange(city: String) {
        _uiState.update { it.copy(city = city) }
    }

    fun onStateChange(state: String) {
        _uiState.update { it.copy(state = state) }
    }

    fun onZipCodeChange(zipCode: String) {
        _uiState.update { it.copy(zipCode = zipCode) }
    }

    fun onCountryChange(country: String) {
        _uiState.update { it.copy(country = country) }
    }

    fun onSaveClick() {
        if (validateForm()) {
            saveProfile()
        }
    }

    fun onCancelClick() {
        // Reset to original values
        val profile = _uiState.value.userProfile
        if (profile != null) {
            _uiState.update { it.copy(
                isEditMode = false,
                fullName = profile.fullName,
                email = profile.email,
                phoneNumber = profile.phoneNumber,
                street = profile.address.street,
                city = profile.address.city,
                state = profile.address.state,
                zipCode = profile.address.zipCode,
                country = profile.address.country,
                fullNameError = null,
                emailError = null,
                phoneError = null
            )}
        }
    }

    fun onChangePasswordClick() {
        _events.value = ProfileEvent.NavigateToChangePassword
    }

    fun onLogoutClick() {
        _events.value = ProfileEvent.ShowLogoutConfirmation
    }

    fun onLogoutConfirmed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                authRepository.signOut()
                _events.value = ProfileEvent.NavigateToLogin
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _events.value = ProfileEvent.ShowMessage("Logout failed: ${e.message}")
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true
        val state = _uiState.value

        // Validate full name
        if (state.fullName.isBlank()) {
            _uiState.update { it.copy(fullNameError = "Name is required") }
            isValid = false
        }

        // Validate phone (optional but if provided, should be valid)
        if (state.phoneNumber.isNotBlank() && state.phoneNumber.length < 10) {
            _uiState.update { it.copy(phoneError = "Invalid phone number") }
            isValid = false
        }

        return isValid
    }

    private fun saveProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            try {
                // Update profile in Supabase (email is not updated)
                val result = authRepository.updateProfile(
                    fullName = _uiState.value.fullName,
                    phone = _uiState.value.phoneNumber.ifBlank { null }
                )
                
                if (result.isSuccess) {
                    // Update the local profile
                    val currentProfile = _uiState.value.userProfile
                    if (currentProfile != null) {
                        val updatedProfile = currentProfile.copy(
                            fullName = _uiState.value.fullName,
                            phoneNumber = _uiState.value.phoneNumber,
                            address = Address(
                                street = _uiState.value.street,
                                city = _uiState.value.city,
                                state = _uiState.value.state,
                                zipCode = _uiState.value.zipCode,
                                country = _uiState.value.country
                            ),
                            updatedAt = System.currentTimeMillis()
                        )
                        
                        _uiState.update { it.copy(
                            isSaving = false,
                            isEditMode = false,
                            userProfile = updatedProfile
                        )}
                        
                        _events.value = ProfileEvent.ShowMessage("Profile updated successfully")
                    }
                } else {
                    _uiState.update { it.copy(isSaving = false) }
                    val errorMessage = result.exceptionOrNull()?.message ?: "Unknown error"
                    _events.value = ProfileEvent.ShowMessage("Failed to update profile: $errorMessage")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
                _events.value = ProfileEvent.ShowMessage("Error updating profile: ${e.message}")
            }
        }
    }

    fun onEventHandled() {
        _events.value = null
    }
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false,
    val userProfile: UserProfile? = null,
    
    // Editable fields
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val street: String = "",
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",
    val country: String = "",
    
    // Errors
    val fullNameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null
)

sealed class ProfileEvent {
    data class ShowMessage(val message: String) : ProfileEvent()
    object NavigateToChangePassword : ProfileEvent()
    object NavigateToLogin : ProfileEvent()
    object ShowLogoutConfirmation : ProfileEvent()
}
