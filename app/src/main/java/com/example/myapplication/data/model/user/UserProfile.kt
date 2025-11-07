package com.example.myapplication.data.model.user

/**
 * User profile data model supporting both Donor and Orphanage users
 */
data class UserProfile(
    val id: String,
    val email: String,
    val userType: UserType,
    val fullName: String,
    val phoneNumber: String = "",
    val profileImageUrl: String? = null,
    val address: Address = Address(),
    
    // Donor-specific fields
    val donorPreferences: DonorPreferences? = null,
    
    // Orphanage-specific fields
    val orphanageInfo: OrphanageInfo? = null,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class UserType {
    DONOR,
    ORPHANAGE
}

data class Address(
    val street: String = "",
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",
    val country: String = ""
) {
    fun toDisplayString(): String {
        val parts = listOfNotNull(
            street.takeIf { it.isNotBlank() },
            city.takeIf { it.isNotBlank() },
            state.takeIf { it.isNotBlank() },
            zipCode.takeIf { it.isNotBlank() }
        )
        return parts.joinToString(", ")
    }
}

data class DonorPreferences(
    val preferredCategories: List<String> = emptyList(),
    val notificationsEnabled: Boolean = true,
    val monthlyDonationGoal: Double? = null
)

data class OrphanageInfo(
    val organizationName: String = "",
    val registrationNumber: String = "",
    val numberOfChildren: Int = 0,
    val description: String = "",
    val website: String = "",
    val establishedYear: Int? = null,
    val verificationStatus: VerificationStatus = VerificationStatus.PENDING
)

enum class VerificationStatus {
    PENDING,
    VERIFIED,
    REJECTED
}
