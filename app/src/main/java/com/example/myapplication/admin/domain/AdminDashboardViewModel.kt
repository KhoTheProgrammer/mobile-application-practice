package com.example.myapplication.admin.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.admin.data.AdminRepository
import com.example.myapplication.admin.data.DashboardStats
import kotlinx.coroutines.launch

data class AdminDashboardUiState(
    val isLoading: Boolean = false,
    val stats: DashboardStats = DashboardStats(),
    val error: String? = null
)

class AdminDashboardViewModel : ViewModel() {
    private val repository = AdminRepository()
    var uiState by mutableStateOf(AdminDashboardUiState())
        private set

    init {
        loadDashboardStats()
    }

    fun loadDashboardStats() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            
            repository.getDashboardStats()
                .onSuccess { stats ->
                    uiState = uiState.copy(
                        isLoading = false,
                        stats = stats
                    )
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load dashboard stats"
                    )
                }
        }
    }

    fun refresh() {
        loadDashboardStats()
    }
}
