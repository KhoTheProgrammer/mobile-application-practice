package com.example.myapplication.auth.data

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val userType: UserType,
    val fullName: String,
    val phone: String? = null,
    val avatarUrl: String? = null,
    val createdAt: String? = null
)

enum class UserType {
    DONOR,
    ORPHANAGE,
    ADMIN
}

@Serializable
data class Profile(
    val id: String,
    val user_type: String,
    val full_name: String,
    val email: String,
    val phone: String? = null,
    val avatar_url: String? = null,
    val created_at: String? = null
)

@Serializable
data class DonorProfile(
    val id: String,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    val postal_code: String? = null,
    val total_donations_made: Double = 0.0,
    val total_donations_count: Int = 0
)

@Serializable
data class OrphanageProfile(
    val id: String,
    val orphanage_name: String,
    val description: String? = null,
    val address: String,
    val city: String,
    val state: String,
    val country: String,
    val postal_code: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val contact_phone: String? = null,
    val contact_email: String? = null,
    val website: String? = null,
    val registration_number: String? = null,
    val number_of_children: Int = 0,
    val total_donations_received: Double = 0.0,
    val rating: Double = 0.0,
    val rating_count: Int = 0,
    val image_url: String? = null,
    val verified: Boolean = false
)
