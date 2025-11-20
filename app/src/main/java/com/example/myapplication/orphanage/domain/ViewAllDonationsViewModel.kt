package com.example.myapplication.orphanage.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.orphanage.data.Donation
import com.example.myapplication.orphanage.data.DonationRepository
import com.example.myapplication.orphanage.data.DonationResult
import com.example.myapplication.orphanage.data.DonationStatistics
import com.example.myapplication.orphanage.data.DonationStatus
import com.example.myapplication.orphanage.data.DonationType
import com.example.myapplication.orphanage.data.DonorSummary
import kotlinx.coroutines.launch

data class ViewAllDonationsUiState(
    val isLoading: Boolean = false,
    val donations: List<Donation> = emptyList(),
    val filteredDonations: List<Donation> = emptyList(),
    val statistics: DonationStatistics? = null,
    val topDonors: List<DonorSummary> = emptyList(),
    val selectedFilter: DonationFilterType = DonationFilterType.ALL,
    val error: String? = null
)

enum class DonationFilterType {
    ALL,
    PENDING,
    CONFIRMED,
    COMPLETED,
    MONETARY,
    IN_KIND
}

class ViewAllDonationsViewModel(
    private val orphanageId: String
) : ViewModel() {
    private val donationRepository = DonationRepository()

    var uiState by mutableStateOf(ViewAllDonationsUiState())
        private set

    init {
        loadDonations()
        loadStatistics()
        loadTopDonors()
    }

    private fun loadDonations() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            
            when (val result = donationRepository.getDonationsByOrphanage(orphanageId)) {
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
            when (val result = donationRepository.getOrphanageStatistics(orphanageId)) {
                is DonationResult.Success -> {
                    uiState = uiState.copy(statistics = result.data)
                }
                is DonationResult.Error -> {
                    // Statistics are optional, don't show error
                }
            }
        }
    }

    private fun loadTopDonors() {
        viewModelScope.launch {
            when (val result = donationRepository.getTopDonors(orphanageId, limit = 10)) {
                is DonationResult.Success -> {
                    uiState = uiState.copy(topDonors = result.data)
                }
                is DonationResult.Error -> {
                    // Top donors are optional, don't show error
                }
            }
        }
    }

    fun filterDonations(filter: DonationFilterType) {
        uiState = uiState.copy(selectedFilter = filter)
        applyFilter(filter)
    }

    private fun applyFilter(filter: DonationFilterType) {
        val filtered = when (filter) {
            DonationFilterType.ALL -> uiState.donations
            DonationFilterType.PENDING -> uiState.donations.filter { 
                it.status == DonationStatus.PENDING
            }
            DonationFilterType.CONFIRMED -> uiState.donations.filter { 
                it.status == DonationStatus.CONFIRMED
            }
            DonationFilterType.COMPLETED -> uiState.donations.filter { 
                it.status == DonationStatus.COMPLETED
            }
            DonationFilterType.MONETARY -> uiState.donations.filter { 
                it.donationType == DonationType.MONETARY 
            }
            DonationFilterType.IN_KIND -> uiState.donations.filter { 
                it.donationType == DonationType.IN_KIND 
            }
        }
        
        uiState = uiState.copy(filteredDonations = filtered)
    }

    fun confirmDonation(donationId: String) {
        viewModelScope.launch {
            android.util.Log.d("ViewAllDonationsVM", "confirmDonation called for: $donationId")
            uiState = uiState.copy(isLoading = true, error = null)

            when (val result = donationRepository.confirmDonation(donationId)) {
                is DonationResult.Success -> {
                    android.util.Log.d("ViewAllDonationsVM", "Donation confirmed successfully")
                    uiState = uiState.copy(
                        isLoading = false,
                        error = null
                    )
                    // Reload donations to reflect the change
                    loadDonations()
                    loadStatistics()
                }
                is DonationResult.Error -> {
                    android.util.Log.e("ViewAllDonationsVM", "Failed to confirm donation: ${result.message}")
                    uiState = uiState.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun completeDonation(donationId: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            when (val result = donationRepository.completeDonation(donationId)) {
                is DonationResult.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        error = null
                    )
                    // Reload donations to reflect the change
                    loadDonations()
                    loadStatistics()
                    loadTopDonors()
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

    fun cancelDonation(donationId: String) {
        viewModelScope.launch {
            android.util.Log.d("ViewAllDonationsVM", "cancelDonation called for: $donationId")
            uiState = uiState.copy(isLoading = true, error = null)

            when (val result = donationRepository.cancelDonation(donationId)) {
                is DonationResult.Success -> {
                    android.util.Log.d("ViewAllDonationsVM", "Donation cancelled successfully")
                    uiState = uiState.copy(
                        isLoading = false,
                        error = null
                    )
                    // Reload donations to reflect the change
                    loadDonations()
                    loadStatistics()
                }
                is DonationResult.Error -> {
                    android.util.Log.e("ViewAllDonationsVM", "Failed to cancel donation: ${result.message}")
                    uiState = uiState.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun refresh() {
        loadDonations()
        loadStatistics()
        loadTopDonors()
    }

    fun clearError() {
        uiState = uiState.copy(error = null)
    }
}
