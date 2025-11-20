package com.example.myapplication.orphanage.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.core.ui.theme.MyApplicationTheme
import com.example.myapplication.core.ui.components.CustomAppBar
import com.example.myapplication.core.ui.components.AppBarAction
import com.example.myapplication.core.ui.getIconForCategory

// Data class for incoming donations to orphanage
data class IncomingDonation(
    val id: String,
    val donorName: String,
    val donorPhone: String,
    val itemCategory: String,
    val itemSubcategory: String,
    val itemDescription: String,
    val condition: String,
    val status: DonationStatus,
    val submittedDate: String,
    val estimatedDelivery: String,
    val pickupAddress: String,
    val willDropOff: Boolean,
    val priority: Priority = Priority.NORMAL
)

enum class DonationStatus(val displayName: String, val color: Color) {
    PENDING("Pending", Color(0xFFFF9800)),
    IN_TRANSIT("In Transit", Color(0xFF2196F3)),
    RECEIVED("Received", Color(0xFF4CAF50)),
    CANCELLED("Cancelled", Color(0xFFF44336))
}

enum class Priority(val displayName: String, val color: Color) {
    LOW("Low", Color(0xFF4CAF50)),
    NORMAL("Normal", Color(0xFF2196F3)),
    HIGH("High", Color(0xFFFF9800)),
    URGENT("Urgent", Color(0xFFF44336))
}

class ViewAllDonationsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val orphanageId = intent.getStringExtra("orphanageId") ?: ""
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ViewAllDonationsScreen(orphanageId = orphanageId)
                }
            }
        }
    }
}

@Composable
fun FiltersSection(
    selectedStatusFilter: String,
    onStatusFilterChange: (String) -> Unit,
    selectedCategoryFilter: String,
    onCategoryFilterChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Status Filter
            Text(
                text = "Status",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val statusOptions = listOf("All", "Pending", "In Transit", "Received", "Cancelled")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                items(statusOptions) { status ->
                    FilterChip(
                        onClick = { onStatusFilterChange(status) },
                        label = { Text(status) },
                        selected = selectedStatusFilter == status
                    )
                }
            }

            // Category Filter
            Text(
                text = "Category",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val categoryOptions =
                listOf("All", "Food", "Clothes", "Books", "Toys", "Electronics", "Others")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categoryOptions) { category ->
                    FilterChip(
                        onClick = { onCategoryFilterChange(category) },
                        label = { Text(category) },
                        selected = selectedCategoryFilter == category
                    )
                }
            }
        }
    }
}

@Composable
fun IncomingDonationCard(
    donation: IncomingDonation,
    onAccept: () -> Unit = {},
    onDecline: () -> Unit = {}
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
            // Header with donor info and priority
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Donor Avatar
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = donation.donorName.first().toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = donation.donorName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = donation.donorPhone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                // Priority Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = donation.priority.color.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = donation.priority.displayName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = donation.priority.color,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Item details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = getIconForCategory(
                        donation.itemCategory
                    ),
                    contentDescription = donation.itemCategory,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${donation.itemCategory} - ${donation.itemSubcategory}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = donation.itemDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Status and delivery info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = donation.status.color.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = donation.status.displayName,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = donation.status.color,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Delivery info
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (donation.willDropOff) Icons.Default.LocalShipping else Icons.Default.LocationOn,
                        contentDescription = "Delivery method",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (donation.willDropOff) "Drop-off" else "Pickup",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons (only for pending donations)
            if (donation.status == DonationStatus.PENDING) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDecline,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Decline")
                    }

                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Accept")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewAllDonationsScreen(
    orphanageId: String,
    viewModel: com.example.myapplication.orphanage.domain.ViewAllDonationsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return com.example.myapplication.orphanage.domain.ViewAllDonationsViewModel(orphanageId) as T
            }
        }
    ),
    onBackClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("All") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var showFilters by remember { mutableStateOf(false) }

    val uiState = viewModel.uiState
    
    // Convert real donations to display format
    val displayDonations = remember(uiState.donations) {
        uiState.donations.map { donation ->
            IncomingDonation(
                id = donation.id,
                donorName = "Donor #${donation.donorId.take(8)}",
                donorPhone = "", // Not available in current data model
                itemCategory = donation.categoryName.ifEmpty { donation.categoryId },
                itemSubcategory = donation.donationType.name,
                itemDescription = donation.itemDescription ?: "${donation.amount} ${donation.currency}",
                condition = "Good", // Not available in current data model
                status = when (donation.status) {
                    com.example.myapplication.orphanage.data.DonationStatus.PENDING -> DonationStatus.PENDING
                    com.example.myapplication.orphanage.data.DonationStatus.CONFIRMED -> DonationStatus.IN_TRANSIT
                    com.example.myapplication.orphanage.data.DonationStatus.COMPLETED -> DonationStatus.RECEIVED
                    com.example.myapplication.orphanage.data.DonationStatus.CANCELLED -> DonationStatus.CANCELLED
                },
                submittedDate = donation.createdAt ?: "",
                estimatedDelivery = "", // Not available
                pickupAddress = "", // Not available
                willDropOff = false,
                priority = Priority.NORMAL
            )
        }
    }

    // Filter donations
    val filteredDonations =
        remember(searchQuery, selectedStatusFilter, selectedCategoryFilter, displayDonations) {
            displayDonations.filter { donation ->
                val matchesSearch = if (searchQuery.isBlank()) true else {
                    donation.donorName.contains(searchQuery, ignoreCase = true) ||
                            donation.itemCategory.contains(searchQuery, ignoreCase = true) ||
                            donation.itemDescription.contains(searchQuery, ignoreCase = true)
                }

                val matchesStatus = selectedStatusFilter == "All" ||
                        donation.status.displayName == selectedStatusFilter

                val matchesCategory = selectedCategoryFilter == "All" ||
                        donation.itemCategory == selectedCategoryFilter

                matchesSearch && matchesStatus && matchesCategory
            }
        }

    Scaffold(
        topBar = {
            CustomAppBar(
                title = "All Donations",
                subtitle = "${filteredDonations.size} donations found",
                onNavigationClick = onBackClick,
                actions = listOf(
                    AppBarAction(
                        icon = if (showFilters) Icons.Default.FilterListOff else Icons.Default.FilterList,
                        contentDescription = "Toggle Filters",
                        onClick = { showFilters = !showFilters }
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
                placeholder = { Text("Search donations, donors, or items...") },
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

            // Filters Section
            if (showFilters) {
                FiltersSection(
                    selectedStatusFilter = selectedStatusFilter,
                    onStatusFilterChange = { selectedStatusFilter = it },
                    selectedCategoryFilter = selectedCategoryFilter,
                    onCategoryFilterChange = { selectedCategoryFilter = it }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Donations List
            if (filteredDonations.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = "No donations found",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No donations found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Try adjusting your search or filters",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredDonations) { donation ->
                        IncomingDonationCard(
                            donation = donation,
                            onAccept = {
                                android.util.Log.d("ViewAllDonations", "Accept clicked for donation: ${donation.id}")
                                viewModel.confirmDonation(donation.id)
                            },
                            onDecline = {
                                android.util.Log.d("ViewAllDonations", "Decline clicked for donation: ${donation.id}")
                                viewModel.cancelDonation(donation.id)
                            }
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ViewAllDonationsScreenPreview() {
    MyApplicationTheme {
        ViewAllDonationsScreen(orphanageId = "preview-orphanage-id")
    }
}
