package com.example.myapplication.ui.orphanage

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
import com.example.myapplication.ui.theme.MyApplicationTheme

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
    onViewAllDonations: () -> Unit = {},
    onUpdateNeeds: () -> Unit = {}
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(horizontal = 24.dp)
    ) {
        item {
            // Header with Orphanage Profile
            OrphanageHeaderSection()
            Spacer(modifier = Modifier.height(32.dp))
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
            // Dashboard Stats - Only donation related
            Text(
                text = "Donation Overview",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            DashboardStatsSection()
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
            RecentDonationsSection(onViewAllDonations = onViewAllDonations)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun OrphanageHeaderSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Orphanage Avatar
        Box(
            modifier = Modifier
                .size(60.dp)
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

        // Notification Icon
        IconButton(
            onClick = { /* Handle notifications */ }
        ) {
            Badge(
                containerColor = MaterialTheme.colorScheme.error
            ) {
                Text("3")
            }
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun DashboardStatsSection() {
    val stats = listOf(
        DashboardStat(
            title = "Pending Donations",
            value = "12",
            icon = Icons.Default.Pending,
            color = Color(0xFFFF9800),
            trend = "+3 today"
        ),
        DashboardStat(
            title = "This Month",
            value = "28",
            icon = Icons.Default.TrendingUp,
            color = Color(0xFF4CAF50),
            trend = "donations"
        ),
        DashboardStat(
            title = "Urgent Needs",
            value = "5",
            icon = Icons.Default.PriorityHigh,
            color = Color(0xFFF44336)
        ),
        DashboardStat(
            title = "Total Received",
            value = "156",
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
fun RecentDonationsSection(onViewAllDonations: () -> Unit = {}) {
    val recentDonations = listOf(
        RecentDonation(
            donorName = "John Doe",
            itemCategory = "Food",
            itemDescription = "Rice and cooking oil",
            timeAgo = "2 hours ago",
            status = "Received",
            statusColor = Color(0xFF4CAF50)
        ),
        RecentDonation(
            donorName = "Sarah Wilson",
            itemCategory = "Clothes",
            itemDescription = "Children's winter clothes",
            timeAgo = "5 hours ago",
            status = "In Transit",
            statusColor = Color(0xFF2196F3)
        ),
        RecentDonation(
            donorName = "Mike Johnson",
            itemCategory = "Books",
            itemDescription = "Educational textbooks",
            timeAgo = "1 day ago",
            status = "Pending",
            statusColor = Color(0xFFFF9800)
        )
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        recentDonations.forEach { donation ->
            RecentDonationCard(donation = donation)
        }

        // View All Button
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

@Preview(showBackground = true)
@Composable
fun OrphanageHomeScreenPreview() {
    MyApplicationTheme {
        OrphanageHomeScreen()
    }
}