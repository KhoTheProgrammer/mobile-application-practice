package com.example.myapplication.orphanage.data

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
