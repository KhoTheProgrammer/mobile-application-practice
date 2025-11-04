package com.example.myapplication.ui.orphanage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.model.donor.OrphanageNeed
import com.example.myapplication.ui.theme.MyApplicationTheme

// Data class for orphanage needs


enum class Urgency(val displayName: String, val color: Color) {
    LOW("Low Priority", Color(0xFF4CAF50)),
    MEDIUM("Medium Priority", Color(0xFFFF9800)),
    HIGH("High Priority", Color(0xFFF44336)),
    CRITICAL("Critical", Color(0xFF9C27B0))
}

class UpdateNeedsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    UpdateNeedsScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateNeedsScreen(onBackClick: () -> Unit = {}) {
    var showAddNeedDialog by remember { mutableStateOf(false) }
    var editingNeed by remember { mutableStateOf<OrphanageNeed?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedUrgencyFilter by remember { mutableStateOf("All") }

    // Sample needs data - replace with actual data from your backend/database
    var needs by remember {
        mutableStateOf(
            listOf(
                OrphanageNeed(
                    id = "1",
                    category = "Food",
                    subcategory = "Grains",
                    description = "Rice and maize flour for daily meals",
                    quantity = "100kg monthly",
                    urgency = Urgency.HIGH,
                    dateAdded = "2024-01-10",
                    isActive = true
                ),
                OrphanageNeed(
                    id = "2",
                    category = "Clothes",
                    subcategory = "Children's Clothing",
                    description = "Winter clothes for children aged 5-12",
                    quantity = "50 pieces",
                    urgency = Urgency.CRITICAL,
                    dateAdded = "2024-01-08",
                    isActive = true
                ),
                OrphanageNeed(
                    id = "3",
                    category = "Books",
                    subcategory = "Educational",
                    description = "Primary school textbooks and stationery",
                    quantity = "200 books",
                    urgency = Urgency.MEDIUM,
                    dateAdded = "2024-01-05",
                    isActive = true
                ),
                OrphanageNeed(
                    id = "4",
                    category = "Medical",
                    subcategory = "First Aid",
                    description = "Basic medical supplies and vitamins",
                    quantity = "Monthly supply",
                    urgency = Urgency.HIGH,
                    dateAdded = "2024-01-12",
                    isActive = true
                ),
                OrphanageNeed(
                    id = "5",
                    category = "Furniture",
                    subcategory = "Beds",
                    description = "Bunk beds for dormitory",
                    quantity = "10 beds",
                    urgency = Urgency.LOW,
                    dateAdded = "2024-01-01",
                    isActive = false
                )
            )
        )
    }

    // Filter needs
    val filteredNeeds = remember(searchQuery, selectedUrgencyFilter, needs) {
        needs.filter { need ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                need.category.contains(searchQuery, ignoreCase = true) ||
                need.subcategory.contains(searchQuery, ignoreCase = true) ||
                need.description.contains(searchQuery, ignoreCase = true)
            }
            
            val matchesUrgency = selectedUrgencyFilter == "All" || 
                need.urgency.displayName == selectedUrgencyFilter
            
            matchesSearch && matchesUrgency && need.isActive
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar with back button
        TopAppBar(
            title = { 
                Column {
                    Text("Manage Needs")
                    Text(
                        text = "${filteredNeeds.size} active needs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                // Add Need Button
                IconButton(
                    onClick = { showAddNeedDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Need",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
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
        if (filteredNeeds.isEmpty()) {
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
                items(filteredNeeds) { need ->
                    NeedCard(
                        need = need,
                        onEdit = { editingNeed = it },
                        onDelete = { needToDelete: OrphanageNeed ->
                            needs = needs.filter { it.id != needToDelete.id }
                        }
                    )
                }
            }
        }
    }

    // Add/Edit Need Dialog
    if (showAddNeedDialog || editingNeed != null) {
        AddEditNeedDialog(
            need = editingNeed,
            onDismiss = { 
                showAddNeedDialog = false
                editingNeed = null
            },
            onSave = { newNeed: OrphanageNeed ->
                if (editingNeed != null) {
                    // Edit existing need
                    needs = needs.map { 
                        if (it.id == editingNeed!!.id) newNeed 
                        else it 
                    }
                } else {
                    // Add new need
                    needs = needs + newNeed.copy(id = (needs.size + 1).toString())
                }
                showAddNeedDialog = false
                editingNeed = null
            }
        )
    }
}
}

@Composable
fun UrgencyFilterSection(
    selectedUrgencyFilter: String,
    onUrgencyFilterChange: (String) -> Unit
) {
    val urgencyOptions = listOf("All", "Low Priority", "Medium Priority", "High Priority", "Critical")
    
    LazyColumn {
        item {
            Text(
                text = "Filter by Urgency",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                urgencyOptions.take(3).forEach { urgency ->
                    FilterChip(
                        onClick = { onUrgencyFilterChange(urgency) },
                        label = { Text(urgency, fontSize = 12.sp) },
                        selected = selectedUrgencyFilter == urgency,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                urgencyOptions.drop(3).forEach { urgency ->
                    FilterChip(
                        onClick = { onUrgencyFilterChange(urgency) },
                        label = { Text(urgency, fontSize = 12.sp) },
                        selected = selectedUrgencyFilter == urgency,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Add empty space to balance the row
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun NeedCard(
    need: OrphanageNeed,
    onEdit: (OrphanageNeed) -> Unit,
    onDelete: (OrphanageNeed) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

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
                        imageVector = _root_ide_package_.com.example.myapplication.ui.donor.getIconForCategory(
                            need.category
                        ),
                        contentDescription = need.category,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${need.category} - ${need.subcategory}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Added ${need.dateAdded}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // Urgency Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = need.urgency.color.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = need.urgency.displayName,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = need.urgency.color,
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
                    onClick = { onEdit(need) },
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
                    onClick = { showDeleteDialog = true },
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

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Need") },
            text = { Text("Are you sure you want to delete this need? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(need)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNeedDialog(
    need: OrphanageNeed?,
    onDismiss: () -> Unit,
    onSave: (OrphanageNeed) -> Unit
) {
    var category by remember { mutableStateOf(need?.category ?: "") }
    var subcategory by remember { mutableStateOf(need?.subcategory ?: "") }
    var description by remember { mutableStateOf(need?.description ?: "") }
    var quantity by remember { mutableStateOf(need?.quantity ?: "") }
    var urgency by remember { mutableStateOf(need?.urgency ?: Urgency.MEDIUM) }

    var categoryExpanded by remember { mutableStateOf(false) }
    var urgencyExpanded by remember { mutableStateOf(false) }

    val categories = listOf("Food", "Clothes", "Books", "Medical", "Furniture", "Toys", "Electronics", "Others")
    val urgencyLevels = Urgency.entries.toList()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(if (need == null) "Add New Need" else "Edit Need") 
        },
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
                        value = category,
                        onValueChange = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
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
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Subcategory
                OutlinedTextField(
                    value = subcategory,
                    onValueChange = { subcategory = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Subcategory") },
                    placeholder = { Text("e.g., Grains, Winter Clothes") }
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description") },
                    placeholder = { Text("Detailed description of the need") },
                    maxLines = 3
                )

                // Quantity
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Quantity Needed") },
                    placeholder = { Text("e.g., 50kg, 20 pieces, Monthly supply") }
                )

                // Urgency Dropdown
                ExposedDropdownMenuBox(
                    expanded = urgencyExpanded,
                    onExpandedChange = { urgencyExpanded = !urgencyExpanded }
                ) {
                    OutlinedTextField(
                        value = urgency.displayName,
                        onValueChange = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
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
                                text = { Text(level.displayName) },
                                onClick = {
                                    urgency = level
                                    urgencyExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (category.isNotBlank() && subcategory.isNotBlank() && 
                        description.isNotBlank() && quantity.isNotBlank()) {
                        val newNeed = OrphanageNeed(
                            id = need?.id ?: "",
                            category = category,
                            subcategory = subcategory,
                            description = description,
                            quantity = quantity,
                            urgency = urgency,
                            dateAdded = need?.dateAdded ?: "2024-01-16",
                            isActive = need?.isActive ?: true
                        )
                        onSave(newNeed)
                    }
                },
                enabled = category.isNotBlank() && subcategory.isNotBlank() && 
                         description.isNotBlank() && quantity.isNotBlank()
            ) {
                Text(if (need == null) "Add Need" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun UpdateNeedsScreenPreview() {
    MyApplicationTheme {
        UpdateNeedsScreen()
    }
}
