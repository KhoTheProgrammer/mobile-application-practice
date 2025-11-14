package com.example.myapplication.ui.donor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.orphanages.Orphanage
import com.example.myapplication.data.repository.OrphanageRepository
import com.example.myapplication.data.repository.OrphanageResult
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
        loadOrphanages()
        loadFeaturedOrphanages()
    }

    fun loadOrphanages() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            
            when (val result = orphanageRepository.getAllOrphanages()) {
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
            when (val result = orphanageRepository.getTopRatedOrphanages(limit = 5)) {
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
            
            when (val result = orphanageRepository.searchOrphanages(query)) {
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
