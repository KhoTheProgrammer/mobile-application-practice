package com.example.myapplication.admin.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.admin.data.AdminRepository
import com.example.myapplication.admin.data.UserManagementItem
import kotlinx.coroutines.launch

data class UserManagementUiState(
    val isLoading: Boolean = false,
    val users: List<UserManagementItem> = emptyList(),
    val filteredUsers: List<UserManagementItem> = emptyList(),
    val selectedUserType: String? = null,
    val selectedStatus: String? = null,
    val searchQuery: String = "",
    val error: String? = null,
    val successMessage: String? = null
)

class UserManagementViewModel : ViewModel() {
    private val repository = AdminRepository()
    var uiState by mutableStateOf(UserManagementUiState())
        private set

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            
            repository.getAllUsers()
                .onSuccess { users ->
                    uiState = uiState.copy(
                        isLoading = false,
                        users = users,
                        filteredUsers = applyFilters(users)
                    )
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load users"
                    )
                }
        }
    }

    fun filterByUserType(userType: String?) {
        uiState = uiState.copy(selectedUserType = userType)
        applyAllFilters()
    }

    fun filterByStatus(status: String?) {
        uiState = uiState.copy(selectedStatus = status)
        applyAllFilters()
    }

    fun searchUsers(query: String) {
        uiState = uiState.copy(searchQuery = query)
        applyAllFilters()
    }

    private fun applyAllFilters() {
        uiState = uiState.copy(filteredUsers = applyFilters(uiState.users))
    }

    private fun applyFilters(users: List<UserManagementItem>): List<UserManagementItem> {
        var filtered = users

        // Filter by user type
        uiState.selectedUserType?.let { type ->
            filtered = filtered.filter { it.userType == type }
        }

        // Filter by status
        uiState.selectedStatus?.let { status ->
            filtered = filtered.filter { it.status == status }
        }

        // Filter by search query
        if (uiState.searchQuery.isNotBlank()) {
            val query = uiState.searchQuery.lowercase()
            filtered = filtered.filter {
                it.fullName.lowercase().contains(query) ||
                it.email.lowercase().contains(query)
            }
        }

        return filtered
    }

    fun updateUserStatus(userId: String, status: String) {
        viewModelScope.launch {
            repository.updateUserStatus(userId, status)
                .onSuccess {
                    uiState = uiState.copy(
                        successMessage = "User status updated successfully"
                    )
                    loadUsers()
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        error = error.message ?: "Failed to update user status"
                    )
                }
        }
    }

    fun verifyUser(userId: String, verified: Boolean) {
        viewModelScope.launch {
            repository.verifyUser(userId, verified)
                .onSuccess {
                    uiState = uiState.copy(
                        successMessage = if (verified) "User verified successfully" else "User unverified"
                    )
                    loadUsers()
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        error = error.message ?: "Failed to verify user"
                    )
                }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            repository.deleteUser(userId)
                .onSuccess {
                    uiState = uiState.copy(
                        successMessage = "User deleted successfully"
                    )
                    loadUsers()
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        error = error.message ?: "Failed to delete user"
                    )
                }
        }
    }

    fun clearMessages() {
        uiState = uiState.copy(error = null, successMessage = null)
    }
}
