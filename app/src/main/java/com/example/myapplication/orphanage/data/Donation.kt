package com.example.myapplication.orphanage.data

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

// Donation models for orphanage module
data class Donation(
    val id: String,
    val donorId: String,
    val orphanageId: String,
    val orphanageName: String = "",
    val categoryId: String,
    val categoryName: String = "",
    val needId: String? = null,
    val amount: Double,
    val currency: String = "USD",
    val donationType: DonationType,
    val itemDescription: String? = null,
    val quantity: Int? = null,
    val status: DonationStatus,
    val note: String? = null,
    val isAnonymous: Boolean = false,
    val isRecurring: Boolean = false,
    val recurringFrequency: RecurringFrequency? = null,
    val createdAt: String? = null,
    val completedAt: String? = null
)

enum class DonationType {
    MONETARY,
    IN_KIND
}

enum class DonationStatus {
    PENDING,
    CONFIRMED,
    COMPLETED,
    CANCELLED
}

enum class RecurringFrequency {
    WEEKLY,
    MONTHLY,
    QUARTERLY,
    YEARLY
}

data class DonationStatistics(
    val totalDonations: Int,
    val totalAmount: Double,
    val pendingDonations: Int,
    val completedDonations: Int,
    val monetaryDonations: Int,
    val inKindDonations: Int
)

sealed class DonationResult<out T> {
    data class Success<T>(val data: T) : DonationResult<T>()
    data class Error(val message: String) : DonationResult<Nothing>()
}

data class DonorSummary(
    val donorId: String,
    val totalAmount: Double,
    val donationCount: Int
)
