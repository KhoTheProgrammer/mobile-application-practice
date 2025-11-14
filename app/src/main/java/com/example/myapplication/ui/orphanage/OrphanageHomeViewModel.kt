package com.example.myapplication.ui.orphanage

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.orphanages.Need
import com.example.myapplication.data.repository.Donation
import com.example.myapplication.data.repository.DonationRepository
import com.example.myapplication.data.repository.DonationResult
import com.example.myapplication.data.repository.DonationStatistics
import com.example.myapplication.data.repository.NeedsRepository
import com.example.myapplication.data.repository.NeedsResult
import com.example.myapplication.data.repository.NeedsStatistics
import kotlinx.coroutines.launch

data class OrphanageHomeUiState(
    val isLoading: Boolean = false,
    val needs: List<Need> = emptyList(),
    val recentDonations: List<Donation> = emptyList(),
    val needsStatistics: NeedsStatistics? = null,
    val donationStatistics: DonationStatistics? = null,
    val error: String? = null
)

class OrphanageHomeViewModel(
    private val orphanageId: String
) : ViewModel() {
    private val needsRepository = NeedsRepository()
    private val donationRepository = DonationRepository()

    var uiState by mutableStateOf(OrphanageHomeUiState())
        private set

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        loadNeeds()
        loadRecentDonations()
        loadNeedsStatistics()
        loadDonationStatistics()
    }

    private fun loadNeeds() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            
            when (val result = needsRepository.getNeedsByOrphanage(orphanageId)) {
                is NeedsResult.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        needs = result.data,
                        error = null
                    )
                }
                is NeedsResult.Error -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    private fun loadRecentDonations() {
        viewModelScope.launch {
            when (val result = donationRepository.getRecentDonations(
                orphanageId = orphanageId,
                limit = 5
            )) {
                is DonationResult.Success -> {
                    uiState = uiState.copy(recentDonations = result.data)
                }
                is DonationResult.Error -> {
                    // Recent donations are optional, don't show error
                }
            }
        }
    }

    private fun loadNeedsStatistics() {
        viewModelScope.launch {
            when (val result = needsRepository.getNeedsStatistics(orphanageId)) {
                is NeedsResult.Success -> {
                    uiState = uiState.copy(needsStatistics = result.data)
                }
                is NeedsResult.Error -> {
                    // Statistics are optional
                }
            }
        }
    }

    private fun loadDonationStatistics() {
        viewModelScope.launch {
            when (val result = donationRepository.getOrphanageStatistics(orphanageId)) {
                is DonationResult.Success -> {
                    uiState = uiState.copy(donationStatistics = result.data)
                }
                is DonationResult.Error -> {
                    // Statistics are optional
                }
            }
        }
    }

    fun refresh() {
        loadDashboardData()
    }

    fun clearError() {
        uiState = uiState.copy(error = null)
    }
}
