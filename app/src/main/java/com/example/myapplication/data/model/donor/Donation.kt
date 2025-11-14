package com.example.myapplication.data.model.donor

import com.example.myapplication.data.model.orphanages.Priority

data class OrphanageNeed(
    val id: String,
    val category: String,
    val subcategory: String,
    val description: String,
    val quantity: String,
    val priority: Priority,
    val dateAdded: String,
    val isActive: Boolean = true
)
