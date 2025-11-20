package com.example.myapplication.core.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

fun getIconForCategory(category: String): ImageVector {
    return when (category) {
        "Food" -> Icons.Default.Fastfood
        "Clothes" -> Icons.Default.Checkroom
        "Books" -> Icons.AutoMirrored.Filled.MenuBook
        "Medical" -> Icons.Default.MedicalServices
        "Furniture" -> Icons.Default.Chair
        "Toys" -> Icons.Default.Toys
        "Electronics" -> Icons.Default.ElectricalServices
        else -> Icons.Default.Category
    }
}
