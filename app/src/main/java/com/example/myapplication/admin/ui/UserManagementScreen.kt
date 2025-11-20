package com.example.myapplication.admin.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.admin.data.UserManagementItem
import com.example.myapplication.admin.domain.UserManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    onBackClick: () -> Unit,
    viewModel: UserManagementViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    var showFilterMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Management") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Default.FilterList, "Filter")
                    }
                    IconButton(onClick = { viewModel.loadUsers() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.searchUsers(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search users...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedUserType == "donor",
                    onClick = { 
                        viewModel.filterByUserType(
                            if (uiState.selectedUserType == "donor") null else "donor"
                        )
                    },
                    label = { Text("Donors") }
                )
                FilterChip(
                    selected = uiState.selectedUserType == "orphanage",
                    onClick = { 
                        viewModel.filterByUserType(
                            if (uiState.selectedUserType == "orphanage") null else "orphanage"
                        )
                    },
                    label = { Text("Orphanages") }
                )
                FilterChip(
                    selected = uiState.selectedStatus == "active",
                    onClick = { 
                        viewModel.filterByStatus(
                            if (uiState.selectedStatus == "active") null else "active"
                        )
                    },
                    label = { Text("Active") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // User List
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.filteredUsers) { user ->
                        UserCard(
                            user = user,
                            onStatusChange = { status ->
                                viewModel.updateUserStatus(user.id, status)
                            },
                            onVerify = { verified ->
                                viewModel.verifyUser(user.id, verified)
                            },
                            onDelete = {
                                viewModel.deleteUser(user.id)
                            }
                        )
                    }
                }
            }
        }

        // Show messages
        uiState.error?.let { error ->
            LaunchedEffect(error) {
                kotlinx.coroutines.delay(3000)
                viewModel.clearMessages()
            }
        }

        uiState.successMessage?.let { message ->
            LaunchedEffect(message) {
                kotlinx.coroutines.delay(3000)
                viewModel.clearMessages()
            }
        }
    }
}

@Composable
private fun UserCard(
    user: UserManagementItem,
    onStatusChange: (String) -> Unit,
    onVerify: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, "More options")
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (user.status == "active") "Suspend" else "Activate") },
                        onClick = {
                            onStatusChange(if (user.status == "active") "suspended" else "active")
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (user.verified) "Unverify" else "Verify") },
                        onClick = {
                            onVerify(!user.verified)
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            showDeleteDialog = true
                            showMenu = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { },
                    label = { Text(user.userType.uppercase()) },
                    leadingIcon = {
                        Icon(
                            imageVector = when (user.userType) {
                                "donor" -> Icons.Default.Person
                                "orphanage" -> Icons.Default.Home
                                else -> Icons.Default.AdminPanelSettings
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
                AssistChip(
                    onClick = { },
                    label = { Text(user.status.uppercase()) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when (user.status) {
                            "active" -> MaterialTheme.colorScheme.primaryContainer
                            "suspended" -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                )
                if (user.verified) {
                    AssistChip(
                        onClick = { },
                        label = { Text("VERIFIED") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Verified,
                                null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete User") },
            text = { Text("Are you sure you want to delete ${user.fullName}? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
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
