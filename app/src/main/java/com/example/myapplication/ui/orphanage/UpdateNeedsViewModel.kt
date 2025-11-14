package com.example.myapplication.ui.orphanage

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.orphanages.Need
import com.example.myapplication.data.model.orphanages.Priority
import com.example.myapplication.data.repository.NeedsRepository
import com.example.myapplication.data.repository.NeedsResult
import kotlinx.coroutines.launch

data class UpdateNeedsUiState(
    val isLoading: Boolean = false,
    val needs: List<Need> = emptyList(),
    val isAddingNeed: Boolean = false,
    val isEditingNeed: Boolean = false,
    val editingNeedId: String? = null,
    val error: String? = null,
    val successMessage: String? = null
)

data class NeedFormState(
    val categoryId: String = "",
    val itemName: String = "",
    val quantity: String = "",
    val priority: Priority = Priority.MEDIUM,
    val description: String = "",
    val itemNameError: String? = null,
    val quantityError: String? = null,
    val categoryError: String? = null
)

class UpdateNeedsViewModel(
    private val orphanageId: String
) : ViewModel() {
    private val needsRepository = NeedsRepository()

    var uiState by mutableStateOf(UpdateNeedsUiState())
        private set

    var formState by mutableStateOf(NeedFormState())
        private set

    init {
        loadNeeds()
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

    fun showAddNeedDialog() {
        uiState = uiState.copy(isAddingNeed = true)
        formState = NeedFormState()
    }

    fun hideAddNeedDialog() {
        uiState = uiState.copy(isAddingNeed = false)
        formState = NeedFormState()
    }

    fun showEditNeedDialog(need: Need) {
        uiState = uiState.copy(
            isEditingNeed = true,
            editingNeedId = need.id
        )
        formState = NeedFormState(
            categoryId = need.category,
            itemName = need.item,
            quantity = need.quantity.toString(),
            priority = need.priority,
            description = need.description
        )
    }

    fun hideEditNeedDialog() {
        uiState = uiState.copy(
            isEditingNeed = false,
            editingNeedId = null
        )
        formState = NeedFormState()
    }

    fun onCategoryChange(categoryId: String) {
        formState = formState.copy(
            categoryId = categoryId,
            categoryError = null
        )
    }

    fun onItemNameChange(itemName: String) {
        formState = formState.copy(
            itemName = itemName,
            itemNameError = null
        )
    }

    fun onQuantityChange(quantity: String) {
        // Only allow numbers
        if (quantity.isEmpty() || quantity.matches(Regex("^\\d+$"))) {
            formState = formState.copy(
                quantity = quantity,
                quantityError = null
            )
        }
    }

    fun onPriorityChange(priority: Priority) {
        formState = formState.copy(priority = priority)
    }

    fun onDescriptionChange(description: String) {
        formState = formState.copy(description = description)
    }

    fun createNeed() {
        android.util.Log.d("UpdateNeedsViewModel", "createNeed called")
        if (!validateForm()) {
            android.util.Log.d("UpdateNeedsViewModel", "Form validation failed")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            android.util.Log.d("UpdateNeedsViewModel", "Creating need with orphanageId: $orphanageId")

            val quantity = formState.quantity.toIntOrNull() ?: 0

            when (val result = needsRepository.createNeed(
                orphanageId = orphanageId,
                categoryId = formState.categoryId,
                itemName = formState.itemName,
                quantity = quantity,
                priority = formState.priority,
                description = formState.description
            )) {
                is NeedsResult.Success -> {
                    android.util.Log.d("UpdateNeedsViewModel", "Need created successfully")
                    uiState = uiState.copy(
                        isLoading = false,
                        isAddingNeed = false,
                        successMessage = "Need created successfully",
                        error = null
                    )
                    formState = NeedFormState()
                    loadNeeds()
                }
                is NeedsResult.Error -> {
                    android.util.Log.e("UpdateNeedsViewModel", "Failed to create need: ${result.message}")
                    uiState = uiState.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun updateNeed() {
        if (!validateForm()) {
            return
        }

        val needId = uiState.editingNeedId ?: return

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            val quantity = formState.quantity.toIntOrNull() ?: 0

            when (val result = needsRepository.updateNeed(
                needId = needId,
                itemName = formState.itemName,
                quantity = quantity,
                priority = formState.priority,
                description = formState.description
            )) {
                is NeedsResult.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        isEditingNeed = false,
                        editingNeedId = null,
                        successMessage = "Need updated successfully",
                        error = null
                    )
                    formState = NeedFormState()
                    loadNeeds()
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

    fun deleteNeed(needId: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            when (val result = needsRepository.deleteNeed(needId)) {
                is NeedsResult.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        successMessage = "Need deleted successfully",
                        error = null
                    )
                    loadNeeds()
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

    fun markNeedAsFulfilled(needId: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            when (val result = needsRepository.markNeedAsFulfilled(needId)) {
                is NeedsResult.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        successMessage = "Need marked as fulfilled",
                        error = null
                    )
                    loadNeeds()
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

    private fun validateForm(): Boolean {
        var isValid = true

        if (formState.categoryId.isBlank()) {
            formState = formState.copy(categoryError = "Please select a category")
            isValid = false
        }

        if (formState.itemName.isBlank()) {
            formState = formState.copy(itemNameError = "Please enter item name")
            isValid = false
        }

        val quantity = formState.quantity.toIntOrNull()
        if (quantity == null || quantity <= 0) {
            formState = formState.copy(quantityError = "Please enter a valid quantity")
            isValid = false
        }

        return isValid
    }

    fun clearError() {
        uiState = uiState.copy(error = null)
    }

    fun clearSuccessMessage() {
        uiState = uiState.copy(successMessage = null)
    }

    fun refresh() {
        loadNeeds()
    }
}
