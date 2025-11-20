package com.example.myapplication.profile.data

import com.example.myapplication.auth.data.UserType

data class UserProfile(
    val id: String,
    val email: String,
    val userType: UserType,
    val fullName: String,
    val phoneNumber: String,
    val address: Address,
    val updatedAt: Long? = null
)

data class Address(
    val street: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String
)
