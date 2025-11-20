package com.example.myapplication.donor.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.donor.data.Donation
import com.example.myapplication.donor.data.DonationRepository
import com.example.myapplication.donor.data.DonationResult
import com.example.myapplication.donor.data.DonationStatistics
import com.example.myapplication.donor.data.DonationStatus
import kotlinx.coroutines.launch

data class ViewMyDonationsUiState(
    val isLoading: Boolean = false,
    val donations: List<Donation> = emptyList(),
    val filteredDonations: List<Donation> = emptyList(),
    val statistics: DonationStatistics? = null,
    val selectedFilter: DonationFilter = DonationFilter.ALL,
    val error: String? = null
)

enum class DonationFilter {
    ALL,
    PENDING,
    COMPLETED,
    RECURRING
}

class ViewMyDonationsViewModel(
    private val donorId: String
) : ViewModel() {
    private val donationRepository = DonationRepository()

    var uiState by mutableStateOf(ViewMyDonationsUiState())
        private set

    init {
        loadDonations()
        loadStatistics()
    }

    private fun loadDonations() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            
            when (val result = donationRepository.getDonationsByDonor(donorId)) {
                is DonationResult.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        donations = result.data,
                        error = null
                    )
                    applyFilter(uiState.selectedFilter)
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

    private fun loadStatistics() {
        viewModelScope.launch {
            when (val result = donationRepository.getDonorStatistics(donorId)) {
                is DonationResult.Success -> {
                    uiState = uiState.copy(statistics = result.data)
                }
                is DonationResult.Error -> {
                    // Statistics are optional, don't show error
                }
            }
        }
    }

    fun filterDonations(filter: DonationFilter) {
        uiState = uiState.copy(selectedFilter = filter)
        applyFilter(filter)
    }

    private fun applyFilter(filter: DonationFilter) {
        val filtered = when (filter) {
            DonationFilter.ALL -> uiState.donations
            DonationFilter.PENDING -> uiState.donations.filter { 
                it.status == DonationStatus.PENDING 
            }
            DonationFilter.COMPLETED -> uiState.donations.filter { 
                it.status == DonationStatus.COMPLETED 
            }
            DonationFilter.RECURRING -> uiState.donations.filter { 
                it.isRecurring 
            }
        }
        
        uiState = uiState.copy(filteredDonations = filtered)
    }

    fun cancelDonation(donationId: String) {
        viewModelScope.launch {
            when (val result = donationRepository.cancelDonation(donationId)) {
                is DonationResult.Success -> {
                    // Reload donations to reflect the change
                    loadDonations()
                    loadStatistics()
                }
                is DonationResult.Error -> {
                    uiState = uiState.copy(error = result.message)
                }
            }
        }
    }

    fun refresh() {
        loadDonations()
        loadStatistics()
    }

    fun clearError() {
        uiState = uiState.copy(error = null)
    }
}
