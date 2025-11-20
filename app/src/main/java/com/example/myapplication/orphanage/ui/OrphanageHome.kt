package com.example.myapplication.orphanage.ui

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.ui.theme.MyApplicationTheme
import com.example.myapplication.core.ui.components.CustomAppBar
import com.example.myapplication.core.ui.components.AppBarAction
import com.example.myapplication.orphanage.domain.OrphanageHomeViewModel

// Data classes for orphanage dashboard
data class DashboardStat(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val color: Color,
    val trend: String = ""
)

data class RecentDonation(
    val donorName: String,
    val itemCategory: String,
    val itemDescription: String,
    val timeAgo: String,
    val status: String,
    val statusColor: Color
)

data class QuickAction(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
fun OrphanageHomeScreen(
    orphanageId: String,
    viewModel: OrphanageHomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return OrphanageHomeViewModel(orphanageId) as T
            }
        }
    ),
    onViewAllDonations: () -> Unit = {},
    onUpdateNeeds: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val uiState = viewModel.uiState

    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background
        )
    )

    Scaffold(
        topBar = {
            CustomAppBar(
                title = "Hope Children's Home",
                subtitle = "Blantyre, Malawi",
                onNavigationClick = null,
                actions = listOf(
                    AppBarAction(
                        icon = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        onClick = onNotificationsClick
                    ),
                    AppBarAction(
                        icon = Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        onClick = onProfileClick
                    )
                ),
                showLogout = true,
                onLogoutClick = onLogout
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

        item {
            // Welcome Message
            Text(
                text = "Welcome back!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            Text(
                text = "Manage your donations and needs",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )
        }

        item {
            // Error Message
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }

        item {
            // Dashboard Stats - Only donation related
            Text(
                text = "Donation Overview",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                DashboardStatsSection(
                    needsStatistics = uiState.needsStatistics,
                    donationStatistics = uiState.donationStatistics
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            // Quick Actions
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            QuickActionsSection(
                onViewAllDonations = onViewAllDonations,
                onUpdateNeeds = onUpdateNeeds
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            // Recent Donations
            Text(
                text = "Recent Donations",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (uiState.recentDonations.isEmpty() && !uiState.isLoading) {
                // Empty State
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = "No donations",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No recent donations",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                RecentDonationsSection(
                    donations = uiState.recentDonations,
                    onViewAllDonations = onViewAllDonations
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
    }
}

@Composable
fun OrphanageHeaderSection(
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Orphanage Avatar - Clickable
        IconButton(
            onClick = onProfileClick,
            modifier = Modifier.size(60.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .shadow(8.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Orphanage",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Orphanage Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hope Children's Home",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Blantyre, Malawi",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }

        // Notifications Icon
        IconButton(onClick = onNotificationsClick) {
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.error
                    ) {
                        Text("3")
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        
        // Profile Icon
        IconButton(onClick = onProfileClick) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profile",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun DashboardStatsSection(
    needsStatistics: com.example.myapplication.orphanage.data.NeedsStatistics?,
    donationStatistics: com.example.myapplication.orphanage.data.DonationStatistics?
) {
    val stats = listOf(
        DashboardStat(
            title = "Pending Donations",
            value = donationStatistics?.pendingDonations?.toString() ?: "0",
            icon = Icons.Default.Pending,
            color = Color(0xFFFF9800),
            trend = ""
        ),
        DashboardStat(
            title = "This Month",
            value = donationStatistics?.totalDonations?.toString() ?: "0",
            icon = Icons.Default.TrendingUp,
            color = Color(0xFF4CAF50),
            trend = "donations"
        ),
        DashboardStat(
            title = "Urgent Needs",
            value = needsStatistics?.urgentNeeds?.toString() ?: "0",
            icon = Icons.Default.PriorityHigh,
            color = Color(0xFFF44336)
        ),
        DashboardStat(
            title = "Total Received",
            value = donationStatistics?.completedDonations?.toString() ?: "0",
            icon = Icons.Default.CheckCircle,
            color = MaterialTheme.colorScheme.primary,
            trend = "all time"
        )
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(stats) { stat ->
            DashboardStatCard(stat = stat)
        }
    }
}

@Composable
fun DashboardStatCard(stat: DashboardStat) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(stat.color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = stat.icon,
                        contentDescription = stat.title,
                        tint = stat.color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stat.value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stat.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1
            )

            if (stat.trend.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stat.trend,
                    style = MaterialTheme.typography.labelSmall,
                    color = stat.color,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun QuickActionsSection(
    onViewAllDonations: () -> Unit = {},
    onUpdateNeeds: () -> Unit = {}
) {
    val quickActions = listOf(
        QuickAction(
            title = "View All Donations",
            description = "See incoming donations",
            icon = Icons.Default.Inventory,
            color = MaterialTheme.colorScheme.primary,
            onClick = onViewAllDonations
        ),
        QuickAction(
            title = "Update Needs",
            description = "Manage urgent requirements",
            icon = Icons.Default.Edit,
            color = Color(0xFFFF9800),
            onClick = onUpdateNeeds
        )
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(quickActions) { action ->
            QuickActionCard(action = action)
        }
    }
}

@Composable
fun QuickActionCard(action: QuickAction) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = action.onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(action.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = action.title,
                    tint = action.color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = action.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = action.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 2
            )
        }
    }
}

@Composable
fun RecentDonationsSection(
    donations: List<com.example.myapplication.orphanage.data.Donation>,
    onViewAllDonations: () -> Unit = {}
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        donations.take(3).forEach { donation ->
            RecentDonationCardFromModel(donation = donation)
        }

        // View All Button
        if (donations.isNotEmpty()) {
            OutlinedButton(
                onClick = onViewAllDonations,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "View All Donations",
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "View All",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun RecentDonationCard(donation: RecentDonation) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Donor Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
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

            Spacer(modifier = Modifier.width(16.dp))

            // Donation Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = donation.donorName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${donation.itemCategory} - ${donation.itemDescription}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1
                )
                Text(
                    text = donation.timeAgo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            // Status Badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = donation.statusColor.copy(alpha = 0.1f)
            ) {
                Text(
                    text = donation.status,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = donation.statusColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun RecentDonationCardFromModel(donation: com.example.myapplication.orphanage.data.Donation) {
    val statusColor = when (donation.status) {
        com.example.myapplication.orphanage.data.DonationStatus.COMPLETED -> Color(0xFF4CAF50)
        com.example.myapplication.orphanage.data.DonationStatus.CONFIRMED -> Color(0xFF2196F3)
        com.example.myapplication.orphanage.data.DonationStatus.PENDING -> Color(0xFFFF9800)
        com.example.myapplication.orphanage.data.DonationStatus.CANCELLED -> Color(0xFFF44336)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Donor Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Donor",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Donation Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Donor #${donation.donorId.take(8)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${donation.categoryId} - ${donation.amount}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1
                )
                Text(
                    text = com.example.myapplication.core.utils.DateUtils.formatToDisplayDate(
                        com.example.myapplication.core.utils.DateUtils.parseIso8601Date(donation.createdAt)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            // Status Badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = statusColor.copy(alpha = 0.1f)
            ) {
                Text(
                    text = donation.status.name,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OrphanageHomeScreenPreview() {
    MyApplicationTheme {
        OrphanageHomeScreen(orphanageId = "preview-orphanage-id")
    }
}