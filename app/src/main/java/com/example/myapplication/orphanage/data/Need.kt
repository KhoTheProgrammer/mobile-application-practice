package com.example.myapplication.orphanage.data

import androidx.compose.ui.graphics.Color

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