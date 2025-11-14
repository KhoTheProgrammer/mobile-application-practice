package com.example.myapplication.data.model.orphanages

import androidx.compose.ui.graphics.Color

data class Orphanage(
    val id: String,
    val name: String,
    val description: String = "",
    val address: String = "",
    val distance: String,
    val needs: String,
    val rating: Float,
    val imageUrl: String? = null,
    val contactInfo: ContactInfo = ContactInfo(),
    val currentNeeds: List<Need> = emptyList(),
    val totalDonationsReceived: Double = 0.0,
    val numberOfChildren: Int = 0
)

data class ContactInfo(
    val phone: String = "",
    val email: String = "",
    val website: String = ""
)

data class Need(
    val id: String = "",
    val category: String,
    val item: String,
    val quantity: Int,
    val priority: Priority = Priority.MEDIUM,
    val description: String = "",
    val createdAt: String = ""
)

enum class Priority(val color: Color) {
    LOW(Color(0xFF4CAF50)), 
    MEDIUM(Color(0xFFFF9800)), 
    HIGH(Color(0xFFF44336)), 
    URGENT(Color(0xFF9C27B0))
}