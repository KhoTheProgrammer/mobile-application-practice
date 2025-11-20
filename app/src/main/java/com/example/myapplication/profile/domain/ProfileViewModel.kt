package com.example.myapplication.profile.domain

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.profile.data.Address
import com.example.myapplication.profile.data.UserProfile
import com.example.myapplication.auth.data.UserType
import kotlinx.coroutines.delay
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

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<ProfileEvent?>(null)
    val events: StateFlow<ProfileEvent?> = _events.asStateFlow()

    init {
        loadUserProfile()
    }

    /**
     * Load user profile - in real app, fetch from repository
     */
    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Simulate API call
            delay(1000)
            
            // Mock user profile - replace with actual data from repository
            val mockProfile = UserProfile(
                id = "user123",
                email = "donor@example.com",
                userType = UserType.DONOR,
                fullName = "John Doe",
                phoneNumber = "+1234567890",
                address = Address(
                    street = "123 Main St",
                    city = "New York",
                    state = "NY",
                    zipCode = "10001",
                    country = "USA"
                )
            )
            
            _uiState.update { it.copy(
                isLoading = false,
                userProfile = mockProfile,
                fullName = mockProfile.fullName,
                email = mockProfile.email,
                phoneNumber = mockProfile.phoneNumber,
                street = mockProfile.address.street,
                city = mockProfile.address.city,
                state = mockProfile.address.state,
                zipCode = mockProfile.address.zipCode,
                country = mockProfile.address.country
            )}
        }
    }

    fun onEditModeToggle() {
        _uiState.update { it.copy(isEditMode = !it.isEditMode) }
    }

    fun onFullNameChange(name: String) {
        _uiState.update { it.copy(fullName = name, fullNameError = null) }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
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
            delay(500) // Simulate logout
            _events.value = ProfileEvent.NavigateToLogin
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

        // Validate email
        if (state.email.isBlank()) {
            _uiState.update { it.copy(emailError = "Email is required") }
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _uiState.update { it.copy(emailError = "Invalid email format") }
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
            
            // Simulate API call
            delay(1500)
            
            // Update the profile
            val currentProfile = _uiState.value.userProfile
            if (currentProfile != null) {
                val updatedProfile = currentProfile.copy(
                    fullName = _uiState.value.fullName,
                    email = _uiState.value.email,
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
