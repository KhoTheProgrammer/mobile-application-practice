package com.example.myapplication.ui.donor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.DonationRepository
import com.example.myapplication.data.repository.DonationResult
import com.example.myapplication.data.repository.DonationType
import com.example.myapplication.data.repository.RecurringFrequency
import kotlinx.coroutines.launch

data class DonationFormUiState(
    val isLoading: Boolean = false,
    val donorId: String = "",
    val orphanageId: String = "",
    val orphanageName: String = "",
    val categoryId: String = "",
    val amount: String = "",
    val donationType: DonationType = DonationType.MONETARY,
    val itemDescription: String = "",
    val quantity: String = "",
    val note: String = "",
    val isAnonymous: Boolean = false,
    val isRecurring: Boolean = false,
    val recurringFrequency: RecurringFrequency? = null,
    val error: String? = null,
    val amountError: String? = null,
    val quantityError: String? = null,
    val donationCreated: Boolean = false,
    val createdDonationId: String? = null
)

class DonationFormViewModel(
    private val orphanageId: String,
    private val orphanageName: String,
    private val categoryId: String
) : ViewModel() {
    private val donationRepository = DonationRepository()

    var uiState by mutableStateOf(
        DonationFormUiState(
            orphanageId = orphanageId,
            orphanageName = orphanageName,
            categoryId = categoryId
        )
    )
        private set

    fun setDonorId(donorId: String) {
        uiState = uiState.copy(donorId = donorId)
    }

    fun onAmountChange(amount: String) {
        // Only allow numbers and decimal point
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d*$"))) {
            uiState = uiState.copy(
                amount = amount,
                amountError = null
            )
        }
    }

    fun onDonationTypeChange(type: DonationType) {
        uiState = uiState.copy(donationType = type)
    }

    fun onItemDescriptionChange(description: String) {
        uiState = uiState.copy(itemDescription = description)
    }

    fun onQuantityChange(quantity: String) {
        // Only allow numbers
        if (quantity.isEmpty() || quantity.matches(Regex("^\\d+$"))) {
            uiState = uiState.copy(
                quantity = quantity,
                quantityError = null
            )
        }
    }

    fun onNoteChange(note: String) {
        uiState = uiState.copy(note = note)
    }

    fun onAnonymousChange(isAnonymous: Boolean) {
        uiState = uiState.copy(isAnonymous = isAnonymous)
    }

    fun onRecurringChange(isRecurring: Boolean) {
        uiState = uiState.copy(isRecurring = isRecurring)
    }

    fun onRecurringFrequencyChange(frequency: RecurringFrequency?) {
        uiState = uiState.copy(recurringFrequency = frequency)
    }

    fun submitDonation() {
        if (!validateForm()) {
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            val amount = uiState.amount.toDoubleOrNull() ?: 0.0
            val quantity = if (uiState.quantity.isNotEmpty()) 
                uiState.quantity.toIntOrNull() 
            else 
                null

            when (val result = donationRepository.createDonation(
                donorId = uiState.donorId,
                orphanageId = uiState.orphanageId,
                categoryId = uiState.categoryId,
                amount = amount,
                donationType = uiState.donationType,
                itemDescription = uiState.itemDescription.ifBlank { null },
                quantity = quantity,
                note = uiState.note.ifBlank { null },
                isAnonymous = uiState.isAnonymous,
                isRecurring = uiState.isRecurring,
                recurringFrequency = if (uiState.isRecurring) uiState.recurringFrequency else null
            )) {
                is DonationResult.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        donationCreated = true,
                        createdDonationId = result.data.id,
                        error = null
                    )
                }
                is DonationResult.Error -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Validate amount for monetary donations
        if (uiState.donationType == DonationType.MONETARY) {
            val amount = uiState.amount.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                uiState = uiState.copy(amountError = "Please enter a valid amount")
                isValid = false
            }
        }

        // Validate quantity for in-kind donations
        if (uiState.donationType == DonationType.IN_KIND) {
            if (uiState.itemDescription.isBlank()) {
                uiState = uiState.copy(error = "Please describe the items you're donating")
                isValid = false
            }
            
            val quantity = uiState.quantity.toIntOrNull()
            if (quantity == null || quantity <= 0) {
                uiState = uiState.copy(quantityError = "Please enter a valid quantity")
                isValid = false
            }
        }

        // Validate recurring frequency
        if (uiState.isRecurring && uiState.recurringFrequency == null) {
            uiState = uiState.copy(error = "Please select a recurring frequency")
            isValid = false
        }

        return isValid
    }

    fun clearError() {
        uiState = uiState.copy(error = null)
    }

    fun resetForm() {
        uiState = DonationFormUiState(
            orphanageId = orphanageId,
            orphanageName = orphanageName,
            categoryId = categoryId,
            donorId = uiState.donorId
        )
    }
}
