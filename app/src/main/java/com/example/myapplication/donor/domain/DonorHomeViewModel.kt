package com.example.myapplication.donor.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.donor.data.OrphanageRepository
import com.example.myapplication.donor.data.OrphanageResult
import com.example.myapplication.orphanage.data.Orphanage
import kotlinx.coroutines.launch

data class DonorHomeUiState(
    val isLoading: Boolean = false,
    val orphanages: List<Orphanage> = emptyList(),
    val featuredOrphanages: List<Orphanage> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val error: String? = null
)

class DonorHomeViewModel : ViewModel() {
    private val orphanageRepository = OrphanageRepository()

    var uiState by mutableStateOf(DonorHomeUiState())
        private set

    init {
        refresh()
    }

    fun loadOrphanages() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            val result = orphanageRepository.getAllOrphanages()
            when (result) {
                is OrphanageResult.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        orphanages = result.data,
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

    fun loadFeaturedOrphanages() {
        viewModelScope.launch {
            val result = orphanageRepository.getTopRatedOrphanages(limit = 5)
            when (result) {
                is OrphanageResult.Success -> {
                    uiState = uiState.copy(featuredOrphanages = result.data)
                }
                is OrphanageResult.Error -> {
                    // Featured orphanages are optional, don't show error
                }
            }
        }
    }

    fun searchOrphanages(query: String) {
        uiState = uiState.copy(searchQuery = query)

        if (query.isBlank()) {
            loadOrphanages()
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            val result = orphanageRepository.searchOrphanages(query)
            when (result) {
                is OrphanageResult.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        orphanages = result.data,
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

    fun filterByCategory(categoryId: String?) {
        uiState = uiState.copy(selectedCategory = categoryId)

        if (categoryId == null) {
            loadOrphanages()
            return
        }

        // Filter orphanages by category (would need to implement in repository)
        // For now, just reload all orphanages
        loadOrphanages()
    }

    fun clearSearch() {
        uiState = uiState.copy(searchQuery = "")
        loadOrphanages()
    }

    fun refresh() {
        loadOrphanages()
        loadFeaturedOrphanages()
    }

    fun clearError() {
        uiState = uiState.copy(error = null)
    }
}
