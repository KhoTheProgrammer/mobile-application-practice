package com.example.myapplication.donor.data

import com.example.myapplication.orphanage.data.ContactInfo
import com.example.myapplication.orphanage.data.Need

/**
 * A data class that represents an orphanage.
 *
 * @param id The ID of the orphanage.
 * @param name The name of the orphanage.
 * @param description A description of the orphanage.
 * @param address The address of the orphanage.
 * @param distance The distance to the orphanage from the user's current location.
 * @param needs A summary of the orphanage's needs.
 * @param rating The rating of the orphanage.
 * @param imageUrl The URL of an image of the orphanage.
 * @param contactInfo The contact information for the orphanage.
 * @param currentNeeds A list of the orphanage's current needs.
 * @param totalDonationsReceived The total amount of donations received by the orphanage.
 * @param numberOfChildren The number of children at the orphanage.
 */
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
