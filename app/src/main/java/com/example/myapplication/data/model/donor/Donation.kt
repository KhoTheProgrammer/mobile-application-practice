package com.example.myapplication.data.model.donor

import com.example.myapplication.ui.orphanage.Urgency

data class OrphanageNeed(
    val id: String,
    val category: String,
    val subcategory: String,
    val description: String,
    val quantity: String,
    val urgency: Urgency,
    val dateAdded: String,
    val isActive: Boolean = true
)