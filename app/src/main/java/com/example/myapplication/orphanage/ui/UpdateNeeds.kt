package com.example.myapplication.orphanage.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.ui.components.AppBarAction
import com.example.myapplication.core.ui.components.CustomAppBar
import com.example.myapplication.core.ui.getIconForCategory
import com.example.myapplication.core.ui.theme.MyApplicationTheme
import com.example.myapplication.orphanage.data.Need
import com.example.myapplication.orphanage.data.Priority
import com.example.myapplication.orphanage.domain.NeedFormState
import com.example.myapplication.orphanage.domain.UpdateNeedsViewModel

/**
 * An activity that allows orphanages to update their needs.
 */
class UpdateNeedsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    UpdateNeedsScreen(orphanageId = "dummy_id")
                }
            }
        }
    }
}

/**
 * A screen that allows orphanages to update their needs.
 *
 * @param orphanageId The ID of the orphanage.
 * @param onBackClick A callback to be invoked when the user clicks the back button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateNeedsScreen(
    orphanageId: String,
    onBackClick: () -> Unit = {}
) {
    val viewModel: UpdateNeedsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return UpdateNeedsViewModel(orphanageId) as T
            }
        }
    )

    val uiState = viewModel.uiState
    val formState = viewModel.formState

    var searchQuery by remember { mutableStateOf("") }
    var selectedUrgencyFilter by remember { mutableStateOf("All") }
    var needToDelete by remember { mutableStateOf<Need?>(null) }

    // Show success message
    uiState.successMessage?.let {
        val snackbarHostState = remember { SnackbarHostState() }
        LaunchedEffect(uiState.successMessage) {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }

    // Filter needs
    val filteredNeeds = remember(searchQuery, selectedUrgencyFilter, uiState.needs) {
        uiState.needs.filter { need ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                need.item.contains(searchQuery, ignoreCase = true) ||
                        need.category.contains(searchQuery, ignoreCase = true) ||
                        need.description.contains(searchQuery, ignoreCase = true)
            }

            val matchesUrgency = selectedUrgencyFilter == "All" ||
                    need.priority.name == selectedUrgencyFilter.uppercase().replace(" PRIORITY", "")

            matchesSearch && matchesUrgency
        }
    }

    Scaffold(
        topBar = {
            CustomAppBar(
                title = "Manage Needs",
                subtitle = "${filteredNeeds.size} active needs",
                onNavigationClick = onBackClick,
                actions = listOf(
                    AppBarAction(
                        icon = Icons.Default.Add,
                        contentDescription = "Add Need",
                        onClick = { viewModel.showAddNeedDialog() }
                    )
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("Search needs...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Urgency Filter
            UrgencyFilterSection(
                selectedUrgencyFilter = selectedUrgencyFilter,
                onUrgencyFilterChange = { selectedUrgencyFilter = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Needs List
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (filteredNeeds.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = "No needs found",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No needs found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Try adjusting your search or add a new need",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredNeeds.size) { index ->
                        val need = filteredNeeds[index]
                        NeedCard(
                            need = need,
                            onEdit = { viewModel.showEditNeedDialog(need) },
                            onDelete = { needToDelete = need }
                        )
                    }
                }
            }
        }

        // Add/Edit Need Dialog
        if (uiState.isAddingNeed || uiState.isEditingNeed) {
            AddEditNeedDialog(
                formState = formState,
                isEditing = uiState.isEditingNeed,
                isLoading = uiState.isLoading,
                error = uiState.error,
                onDismiss = {
                    if (uiState.isAddingNeed) viewModel.hideAddNeedDialog() else viewModel.hideEditNeedDialog()
                },
                onSave = {
                    if (uiState.isAddingNeed) viewModel.createNeed() else viewModel.updateNeed()
                },
                onEvent = {
                    when (it) {
                        is NeedFormEvent.OnCategoryChange -> viewModel.onCategoryChange(it.category)
                        is NeedFormEvent.OnItemNameChange -> viewModel.onItemNameChange(it.name)
                        is NeedFormEvent.OnQuantityChange -> viewModel.onQuantityChange(it.quantity)
                        is NeedFormEvent.OnPriorityChange -> viewModel.onPriorityChange(it.priority)
                        is NeedFormEvent.OnDescriptionChange -> viewModel.onDescriptionChange(it.description)
                    }
                }
            )
        }

        needToDelete?.let { need ->
            AlertDialog(
                onDismissRequest = { needToDelete = null },
                title = { Text("Delete Need") },
                text = { Text("Are you sure you want to delete this need? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteNeed(need.id)
                            needToDelete = null
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { needToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

/**
 * A composable that displays a set of urgency filter chips.
 *
 * @param selectedUrgencyFilter The currently selected urgency filter.
 * @param onUrgencyFilterChange A callback to be invoked when the selected urgency filter changes.
 */
@Composable
fun UrgencyFilterSection(
    selectedUrgencyFilter: String,
    onUrgencyFilterChange: (String) -> Unit
) {
    val urgencyOptions = listOf("All", "Low Priority", "Medium Priority", "High Priority", "Critical")

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        urgencyOptions.forEach { urgency ->
            FilterChip(
                onClick = { onUrgencyFilterChange(urgency) },
                label = { Text(urgency, fontSize = 12.sp) },
                selected = selectedUrgencyFilter == urgency
            )
        }
    }
}

/**
 * A composable that displays a single need card.
 *
 * @param need The need to display.
 * @param onEdit A callback to be invoked when the user clicks the edit button.
 * @param onDelete A callback to be invoked when the user clicks the delete button.
 */
@Composable
fun NeedCard(
    need: Need,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header with category and urgency
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = getIconForCategory(need.category),
                        contentDescription = need.category,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = need.item,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Added on ${need.createdAt}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // Urgency Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = need.priority.color.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = need.priority.name.lowercase().replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = need.priority.color,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            Text(
                text = need.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quantity
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Numbers,
                    contentDescription = "Quantity",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Needed: ${need.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Edit Button
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }

                // Delete Button
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }
}

/**
 * An event that can be sent from the [AddEditNeedDialog] to the [UpdateNeedsViewModel].
 */
sealed class NeedFormEvent {
    /**
     * Indicates that the category has changed.
     *
     * @param category The new category.
     */
    data class OnCategoryChange(val category: String) : NeedFormEvent()

    /**
     * Indicates that the item name has changed.
     *
     * @param name The new item name.
     */
    data class OnItemNameChange(val name: String) : NeedFormEvent()

    /**
     * Indicates that the quantity has changed.
     *
     * @param quantity The new quantity.
     */
    data class OnQuantityChange(val quantity: String) : NeedFormEvent()

    /**
     * Indicates that the priority has changed.
     *
     * @param priority The new priority.
     */
    data class OnPriorityChange(val priority: Priority) : NeedFormEvent()

    /**
     * Indicates that the description has changed.
     *
     * @param description The new description.
     */
    data class OnDescriptionChange(val description: String) : NeedFormEvent()
}

/**
 * A dialog that allows the user to add or edit a need.
 *
 * @param formState The current state of the form.
 * @param isEditing Whether the dialog is being used to edit an existing need.
 * @param isLoading Whether the dialog is currently in a loading state.
 * @param error An error message to display, if any.
 * @param onDismiss A callback to be invoked when the dialog is dismissed.
 * @param onSave A callback to be invoked when the user clicks the save button.
 * @param onEvent A callback to be invoked when an event is sent from the dialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNeedDialog(
    formState: NeedFormState,
    isEditing: Boolean,
    isLoading: Boolean = false,
    error: String? = null,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onEvent: (NeedFormEvent) -> Unit
) {
    var categoryExpanded by remember { mutableStateOf(false) }
    var urgencyExpanded by remember { mutableStateOf(false) }

    val categories = listOf("Food", "Clothes", "Books", "Medical", "Furniture", "Toys", "Electronics", "Others")
    val urgencyLevels = Priority.entries

    AlertDialog(
        onDismissRequest = if (isLoading) { {} } else onDismiss,
        title = { Text(if (isEditing) "Edit Need" else "Add New Need") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = formState.categoryId,
                        onValueChange = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryEditable, true),
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                        }
                    )

                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    onEvent(NeedFormEvent.OnCategoryChange(cat))
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Subcategory
                OutlinedTextField(
                    value = formState.itemName,
                    onValueChange = { onEvent(NeedFormEvent.OnItemNameChange(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Item Name") },
                    placeholder = { Text("e.g., Grains, Winter Clothes") },
                    isError = formState.itemNameError != null,
                    supportingText = { formState.itemNameError?.let { Text(it) } }
                )

                // Description
                OutlinedTextField(
                    value = formState.description,
                    onValueChange = { onEvent(NeedFormEvent.OnDescriptionChange(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description") },
                    placeholder = { Text("Detailed description of the need") },
                    maxLines = 3
                )

                // Quantity
                OutlinedTextField(
                    value = formState.quantity,
                    onValueChange = { onEvent(NeedFormEvent.OnQuantityChange(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Quantity Needed") },
                    placeholder = { Text("e.g., 50kg, 20 pieces") },
                    isError = formState.quantityError != null,
                    supportingText = { formState.quantityError?.let { Text(it) } }
                )

                // Urgency Dropdown
                ExposedDropdownMenuBox(
                    expanded = urgencyExpanded,
                    onExpandedChange = { urgencyExpanded = !urgencyExpanded }
                ) {
                    OutlinedTextField(
                        value = formState.priority.name.lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryEditable, true),
                        readOnly = true,
                        label = { Text("Urgency Level") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = urgencyExpanded)
                        }
                    )

                    ExposedDropdownMenu(
                        expanded = urgencyExpanded,
                        onDismissRequest = { urgencyExpanded = false }
                    ) {
                        urgencyLevels.forEach { level ->
                            DropdownMenuItem(
                                text = { Text(level.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    onEvent(NeedFormEvent.OnPriorityChange(level))
                                    urgencyExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Error Message
                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = !isLoading && formState.itemName.isNotBlank() && formState.quantity.isNotBlank() && formState.categoryId.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (isEditing) "Save Changes" else "Add Need")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun UpdateNeedsScreenPreview() {
    MyApplicationTheme {
        UpdateNeedsScreen(orphanageId = "dummy_id")
    }
}
