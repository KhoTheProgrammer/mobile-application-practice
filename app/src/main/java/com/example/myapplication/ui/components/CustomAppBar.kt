package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A beautiful, modern, and reusable AppBar component with navigation and actions
 *
 * @param title The screen title to display
 * @param onNavigationClick Callback for back navigation (null to hide back button)
 * @param actions List of action items to display on the right
 * @param subtitle Optional subtitle text below the title
 * @param showElevation Whether to show elevation shadow
 * @param backgroundColor Custom background color (null for default)
 * @param contentColor Custom content color (null for default)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomAppBar(
    title: String,
    onNavigationClick: (() -> Unit)? = null,
    actions: List<AppBarAction> = emptyList(),
    subtitle: String? = null,
    showElevation: Boolean = true,
    backgroundColor: Color? = null,
    contentColor: Color? = null
) {
    TopAppBar(
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                subtitle?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = (contentColor ?: MaterialTheme.colorScheme.onPrimary).copy(alpha = 0.9f)
                    )
                }
            }
        },
        navigationIcon = {
            onNavigationClick?.let {
                IconButton(onClick = it) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate back"
                    )
                }
            }
        },
        actions = {
            actions.forEach { action ->
                IconButton(
                    onClick = action.onClick,
                    enabled = action.enabled
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.contentDescription,
                        tint = if (action.enabled) {
                            contentColor ?: MaterialTheme.colorScheme.onPrimary
                        } else {
                            (contentColor ?: MaterialTheme.colorScheme.onPrimary).copy(alpha = 0.38f)
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = backgroundColor ?: MaterialTheme.colorScheme.primary,
            titleContentColor = contentColor ?: MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = contentColor ?: MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = contentColor ?: MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier.then(
            if (showElevation) {
                Modifier.shadow(elevation = 4.dp)
            } else {
                Modifier
            }
        )
    )
}

/**
 * Data class representing an action item in the AppBar
 *
 * @param icon The icon to display
 * @param contentDescription Accessibility description
 * @param onClick Callback when the action is clicked
 * @param enabled Whether the action is enabled
 */
data class AppBarAction(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true
)

/**
 * A centered title variant of the AppBar for special screens
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CenteredAppBar(
    title: String,
    onNavigationClick: (() -> Unit)? = null,
    actions: List<AppBarAction> = emptyList(),
    showElevation: Boolean = true,
    backgroundColor: Color? = null,
    contentColor: Color? = null
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            onNavigationClick?.let {
                IconButton(onClick = it) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate back"
                    )
                }
            }
        },
        actions = {
            actions.forEach { action ->
                IconButton(
                    onClick = action.onClick,
                    enabled = action.enabled
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.contentDescription,
                        tint = if (action.enabled) {
                            contentColor ?: MaterialTheme.colorScheme.onPrimary
                        } else {
                            (contentColor ?: MaterialTheme.colorScheme.onPrimary).copy(alpha = 0.38f)
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = backgroundColor ?: MaterialTheme.colorScheme.primary,
            titleContentColor = contentColor ?: MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = contentColor ?: MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = contentColor ?: MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier.then(
            if (showElevation) {
                Modifier.shadow(elevation = 4.dp)
            } else {
                Modifier
            }
        )
    )
}

/**
 * A large title AppBar for main screens with prominent title
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LargeAppBar(
    title: String,
    onNavigationClick: (() -> Unit)? = null,
    actions: List<AppBarAction> = emptyList(),
    backgroundColor: Color? = null,
    contentColor: Color? = null
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    
    LargeTopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            onNavigationClick?.let {
                IconButton(onClick = it) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate back"
                    )
                }
            }
        },
        actions = {
            actions.forEach { action ->
                IconButton(
                    onClick = action.onClick,
                    enabled = action.enabled
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.contentDescription,
                        tint = if (action.enabled) {
                            contentColor ?: MaterialTheme.colorScheme.onPrimary
                        } else {
                            (contentColor ?: MaterialTheme.colorScheme.onPrimary).copy(alpha = 0.38f)
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = backgroundColor ?: MaterialTheme.colorScheme.primary,
            titleContentColor = contentColor ?: MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = contentColor ?: MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = contentColor ?: MaterialTheme.colorScheme.onPrimary
        ),
        scrollBehavior = scrollBehavior
    )
}
