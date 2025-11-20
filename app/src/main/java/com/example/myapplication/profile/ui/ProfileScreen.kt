package com.example.myapplication.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.components.CustomAppBar
import com.example.myapplication.ui.theme.MyApplicationTheme

/**
 * Profile Screen - MVVM Pattern
 * Allows users to view and edit their profile information
 */
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val event by viewModel.events.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Handle events
    LaunchedEffect(event) {
        when (event) {
            is ProfileEvent.ShowMessage -> {
                snackbarHostState.showSnackbar((event as ProfileEvent.ShowMessage).message)
                viewModel.onEventHandled()
            }
            is ProfileEvent.NavigateToChangePassword -> {
                onNavigateToChangePassword()
                viewModel.onEventHandled()
            }
            is ProfileEvent.NavigateToLogin -> {
                onNavigateToLogin()
                viewModel.onEventHandled()
            }
            is ProfileEvent.ShowLogoutConfirmation -> {
                showLogoutDialog = true
                viewModel.onEventHandled()
            }
            null -> { /* No event */ }
        }
    }

    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.Default.Logout, contentDescription = null) },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.onLogoutConfirmed()
                    }
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CustomAppBar(
                title = "Profile",
                onNavigationClick = onNavigateBack,
                actions = if (!uiState.isEditMode) {
                    listOf(
                        com.example.myapplication.ui.components.AppBarAction(
                            icon = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            onClick = viewModel::onEditModeToggle
                        )
                    )
                } else emptyList()
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    // Profile Header
                    ProfileHeader(
                        fullName = uiState.fullName,
                        email = uiState.email,
                        userType = uiState.userProfile?.userType?.name ?: "USER"
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Personal Information Section
                    SectionTitle("Personal Information")
                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileTextField(
                        value = uiState.fullName,
                        onValueChange = viewModel::onFullNameChange,
                        label = "Full Name",
                        icon = Icons.Default.Person,
                        enabled = uiState.isEditMode,
                        error = uiState.fullNameError
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileTextField(
                        value = uiState.email,
                        onValueChange = viewModel::onEmailChange,
                        label = "Email",
                        icon = Icons.Default.Email,
                        enabled = uiState.isEditMode,
                        keyboardType = KeyboardType.Email,
                        error = uiState.emailError
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileTextField(
                        value = uiState.phoneNumber,
                        onValueChange = viewModel::onPhoneNumberChange,
                        label = "Phone Number",
                        icon = Icons.Default.Phone,
                        enabled = uiState.isEditMode,
                        keyboardType = KeyboardType.Phone,
                        error = uiState.phoneError
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Address Section
                    SectionTitle("Address")
                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileTextField(
                        value = uiState.street,
                        onValueChange = viewModel::onStreetChange,
                        label = "Street Address",
                        icon = Icons.Default.Home,
                        enabled = uiState.isEditMode
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProfileTextField(
                            value = uiState.city,
                            onValueChange = viewModel::onCityChange,
                            label = "City",
                            icon = Icons.Default.LocationCity,
                            enabled = uiState.isEditMode,
                            modifier = Modifier.weight(1f)
                        )

                        ProfileTextField(
                            value = uiState.state,
                            onValueChange = viewModel::onStateChange,
                            label = "State",
                            icon = Icons.Default.Map,
                            enabled = uiState.isEditMode,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProfileTextField(
                            value = uiState.zipCode,
                            onValueChange = viewModel::onZipCodeChange,
                            label = "Zip Code",
                            icon = Icons.Default.Pin,
                            enabled = uiState.isEditMode,
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )

                        ProfileTextField(
                            value = uiState.country,
                            onValueChange = viewModel::onCountryChange,
                            label = "Country",
                            icon = Icons.Default.Public,
                            enabled = uiState.isEditMode,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons
                    if (uiState.isEditMode) {
                        // Save and Cancel buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = viewModel::onCancelClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                enabled = !uiState.isSaving
                            ) {
                                Text("Cancel")
                            }

                            Button(
                                onClick = viewModel::onSaveClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                enabled = !uiState.isSaving
                            ) {
                                if (uiState.isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Save Changes")
                                }
                            }
                        }
                    } else {
                        // Other action buttons
                        ProfileActionButton(
                            text = "Change Password",
                            icon = Icons.Default.Lock,
                            onClick = viewModel::onChangePasswordClick
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        ProfileActionButton(
                            text = "Logout",
                            icon = Icons.Default.Logout,
                            onClick = viewModel::onLogoutClick,
                            isDestructive = true
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    fullName: String,
    email: String,
    userType: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = fullName.firstOrNull()?.uppercase() ?: "U",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = fullName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = email,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ) {
                Text(
                    text = userType,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    error: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (enabled) MaterialTheme.colorScheme.primary 
                      else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        },
        enabled = enabled,
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.38f),
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    )
}

@Composable
private fun ProfileActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (isDestructive) 
                MaterialTheme.colorScheme.error 
            else 
                MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 16.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    MyApplicationTheme {
        ProfileScreen()
    }
}
