package com.example.myapplication.ui.donor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.orphanages.Need
import com.example.myapplication.data.model.orphanages.Orphanage
import com.example.myapplication.data.repository.NeedsRepository
import com.example.myapplication.data.repository.NeedsResult
import com.example.myapplication.data.repository.OrphanageRepository
import com.example.myapplication.data.repository.OrphanageResult
import kotlinx.coroutines.launch

data class OrphanageDetailUiState(
    val isLoading: Boolean = false,
    val orphanage: Orphanage? = null,
    val needs: List<Need> = emptyList(),
    val error: String? = null,
    val isFavorite: Boolean = false
)

class OrphanageDetailViewModel(
    private val orphanageId: String
) : ViewModel() {
    private val orphanageRepository = OrphanageRepository()
    private val needsRepository = NeedsRepository()

    var uiState by mutableStateOf(OrphanageDetailUiState())
        private set

    init {
        loadOrphanageDetails()
        loadOrphanageNeeds()
    }

    private fun loadOrphanageDetails() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            
            when (val result = orphanageRepository.getOrphanageById(orphanageId)) {
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

    private fun loadOrphanageNeeds() {
        viewModelScope.launch {
            when (val result = needsRepository.getNeedsByOrphanage(orphanageId)) {
                is NeedsResult.Success -> {
                    uiState = uiState.copy(needs = result.data)
                }
                is NeedsResult.Error -> {
                    // Needs are optional, don't show error
                }
            }
        }
    }

    fun toggleFavorite() {
        // TODO: Implement favorite functionality with repository
        uiState = uiState.copy(isFavorite = !uiState.isFavorite)
    }

    fun refresh() {
        loadOrphanageDetails()
        loadOrphanageNeeds()
    }

    fun clearError() {
        uiState = uiState.copy(error = null)
    }
}
