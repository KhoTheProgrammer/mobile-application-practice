package com.example.myapplication.donor.data

import com.example.myapplication.orphanage.data.Priority

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
