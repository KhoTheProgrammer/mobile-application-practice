package com.example.myapplication.admin.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.admin.data.AdminRepository
import com.example.myapplication.admin.data.OrphanageVerificationItem
import kotlinx.coroutines.launch

data class OrphanageVerificationUiState(
    val isLoading: Boolean = false,
    val pendingOrphanages: List<OrphanageVerificationItem> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

class OrphanageVerificationViewModel(private val adminId: String) : ViewModel() {
    private val repository = AdminRepository()
    var uiState by mutableStateOf(OrphanageVerificationUiState())
        private set

    init {
        loadPendingVerifications()
    }

    fun loadPendingVerifications() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            
            repository.getPendingOrphanageVerifications()
                .onSuccess { orphanages ->
                    uiState = uiState.copy(
                        isLoading = false,
                        pendingOrphanages = orphanages
                    )
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load pending verifications"
                    )
                }
        }
    }

    fun approveOrphanage(orphanageId: String, notes: String? = null) {
        viewModelScope.launch {
            repository.verifyOrphanage(orphanageId, adminId, "verified", notes)
                .onSuccess {
                    repository.logActivity(
                        adminId = adminId,
                        actionType = "ORPHANAGE_VERIFIED",
                        targetType = "ORPHANAGE",
                        targetId = orphanageId,
                        description = "Orphanage verified"
                    )
                    uiState = uiState.copy(
                        successMessage = "Orphanage verified successfully"
                    )
                    loadPendingVerifications()
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        error = error.message ?: "Failed to verify orphanage"
                    )
                }
        }
    }

    fun rejectOrphanage(orphanageId: String, notes: String) {
        viewModelScope.launch {
            repository.verifyOrphanage(orphanageId, adminId, "rejected", notes)
                .onSuccess {
                    repository.logActivity(
                        adminId = adminId,
                        actionType = "ORPHANAGE_REJECTED",
                        targetType = "ORPHANAGE",
                        targetId = orphanageId,
                        description = "Orphanage verification rejected: $notes"
                    )
                    uiState = uiState.copy(
                        successMessage = "Orphanage verification rejected"
                    )
                    loadPendingVerifications()
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        error = error.message ?: "Failed to reject orphanage"
                    )
                }
        }
    }

    fun clearMessages() {
        uiState = uiState.copy(error = null, successMessage = null)
    }
}
