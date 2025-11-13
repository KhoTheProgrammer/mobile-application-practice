package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme

/**
 * Example usage of CustomAppBar components
 * This file demonstrates various configurations and use cases
 */

// Example 1: Basic AppBar with back navigation
@Composable
fun BasicAppBarExample(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            CustomAppBar(
                title = "Orphanage Details",
                onNavigationClick = onBackClick
            )
        }
    ) { paddingValues ->
        // Your screen content here
        Box(modifier = Modifier.padding(paddingValues)) {
            // Content
        }
    }
}

// Example 2: AppBar with subtitle
@Composable
fun AppBarWithSubtitleExample(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            CustomAppBar(
                title = "My Donations",
                subtitle = "Total: 5 donations",
                onNavigationClick = onBackClick
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            // Content
        }
    }
}

// Example 3: AppBar with actions
@Composable
fun AppBarWithActionsExample(
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onFilterClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Scaffold(
        topBar = {
            CustomAppBar(
                title = "Browse Orphanages",
                onNavigationClick = onBackClick,
                actions = listOf(
                    AppBarAction(
                        icon = Icons.Default.Search,
                        contentDescription = "Search",
                        onClick = onSearchClick
                    ),
                    AppBarAction(
                        icon = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        onClick = onFilterClick
                    ),
                    AppBarAction(
                        icon = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        onClick = onMoreClick
                    )
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            // Content
        }
    }
}

// Example 4: AppBar without back button (for main screens)
@Composable
fun MainScreenAppBarExample(
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Scaffold(
        topBar = {
            CustomAppBar(
                title = "Donor Home",
                onNavigationClick = null, // No back button
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
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            // Content
        }
    }
}

// Example 5: Centered AppBar
@Composable
fun CenteredAppBarExample(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            CenteredAppBar(
                title = "Donation Form",
                onNavigationClick = onBackClick
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            // Content
        }
    }
}

// Example 6: AppBar with custom colors
@Composable
fun CustomColorAppBarExample(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            CustomAppBar(
                title = "Special Screen",
                onNavigationClick = onBackClick,
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            // Content
        }
    }
}

// Example 7: AppBar with disabled action
@Composable
fun AppBarWithDisabledActionExample(
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    isSaveEnabled: Boolean
) {
    Scaffold(
        topBar = {
            CustomAppBar(
                title = "Edit Profile",
                onNavigationClick = onBackClick,
                actions = listOf(
                    AppBarAction(
                        icon = Icons.Default.Check,
                        contentDescription = "Save",
                        onClick = onSaveClick,
                        enabled = isSaveEnabled
                    )
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            // Content
        }
    }
}

// Example 8: Large AppBar for main screens
@Composable
fun LargeAppBarExample(
    onMenuClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Scaffold(
        topBar = {
            LargeAppBar(
                title = "Welcome Back!",
                actions = listOf(
                    AppBarAction(
                        icon = Icons.Default.Settings,
                        contentDescription = "Settings",
                        onClick = onSettingsClick
                    )
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            // Content
        }
    }
}

// Preview examples
@Preview(showBackground = true)
@Composable
fun PreviewBasicAppBar() {
    MyApplicationTheme {
        BasicAppBarExample(onBackClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAppBarWithSubtitle() {
    MyApplicationTheme {
        AppBarWithSubtitleExample(onBackClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAppBarWithActions() {
    MyApplicationTheme {
        AppBarWithActionsExample(
            onBackClick = {},
            onSearchClick = {},
            onFilterClick = {},
            onMoreClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCenteredAppBar() {
    MyApplicationTheme {
        CenteredAppBarExample(onBackClick = {})
    }
}
