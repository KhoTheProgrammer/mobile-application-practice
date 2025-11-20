/**
 * This package contains the domain layer for the donor feature.
 */
package com.example.myapplication.donor.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.donor.data.OrphanageRepository
import com.example.myapplication.donor.data.OrphanageResult
import com.example.myapplication.orphanage.data.Need
import com.example.myapplication.orphanage.data.NeedsRepository
import com.example.myapplication.orphanage.data.NeedsResult
import com.example.myapplication.orphanage.data.Orphanage
import kotlinx.coroutines.launch

/**
 * Represents the UI state for the orphanage detail screen.
 *
 * @property isLoading Indicates whether the data is currently being loaded.
 * @property orphanage The orphanage details to be displayed.
 * @property needs The list of needs for the orphanage.
 * @property error An error message to be displayed, if any.
 * @property isFavorite Indicates whether the orphanage is marked as a favorite by the user.
 */
data class OrphanageDetailUiState(
    val isLoading: Boolean = false,
    val orphanage: Orphanage? = null,
    val needs: List<Need> = emptyList(),
    val error: String? = null,
    val isFavorite: Boolean = false
)

/**
 * ViewModel for the Orphanage Detail screen.
 *
 * @param orphanageId The ID of the orphanage to display.
 */
class OrphanageDetailViewModel(
    private val orphanageId: String
) : ViewModel() {
    private val orphanageRepository = OrphanageRepository()
    private val needsRepository = NeedsRepository()

    /**
     * The UI state for the Orphanage Detail screen.
     */
    var uiState by mutableStateOf(OrphanageDetailUiState())
        private set

    init {
        loadOrphanageDetails()
        loadOrphanageNeeds()
    }

    /**
     * Fetches the orphanage details from the repository and updates the UI state.
     */
    private fun loadOrphanageDetails() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            val result = orphanageRepository.getOrphanageById(orphanageId)
            when (result) {
                is OrphanageResult.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        orphanage = result.data,
                        error = null
                    )
                }

                is OrphanageResult.Error -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    /**
     * Fetches the orphanage needs from the repository and updates the UI state.
     */
    private fun loadOrphanageNeeds() {
        viewModelScope.launch {
            val result = needsRepository.getNeedsByOrphanage(orphanageId)
            when (result) {
                is NeedsResult.Success -> {
                    uiState = uiState.copy(needs = result.data)
                }

                is NeedsResult.Error -> {
                    // Needs are optional, so don't show an error
                }
            }
        }
    }

    /**
     * Toggles the favorite status of the orphanage.
     */
    fun toggleFavorite() {
        // TODO: Implement favorite functionality with a repository
        uiState = uiState.copy(isFavorite = !uiState.isFavorite)
    }

    /**
     * Refreshes the orphanage details and needs.
     */
    fun refresh() {
        loadOrphanageDetails()
        loadOrphanageNeeds()
    }

    /**
     * Clears the error message from the UI state.
     */
    fun clearError() {
        uiState = uiState.copy(error = null)
    }
}
