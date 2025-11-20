package com.example.myapplication.admin.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdminProfile(
    val id: String,
    val role: String = "admin",
    val permissions: List<String> = emptyList(),
    val department: String? = null,
    @SerialName("last_login_at") val lastLoginAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class UserManagementItem(
    val id: String,
    @SerialName("user_type") val userType: String,
    @SerialName("full_name") val fullName: String,
    val email: String,
    val phone: String? = null,
    val status: String = "active",
    val verified: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("last_login_at") val lastLoginAt: String? = null
)

@Serializable
data class DashboardStats(
    @SerialName("total_users") val totalUsers: Long = 0,
    @SerialName("total_donors") val totalDonors: Long = 0,
    @SerialName("total_orphanages") val totalOrphanages: Long = 0,
    @SerialName("total_admins") val totalAdmins: Long = 0,
    @SerialName("active_users") val activeUsers: Long = 0,
    @SerialName("suspended_users") val suspendedUsers: Long = 0,
    @SerialName("total_donations_amount") val totalDonationsAmount: Double = 0.0,
    @SerialName("total_donations_count") val totalDonationsCount: Long = 0,
    @SerialName("pending_donations") val pendingDonations: Long = 0,
    @SerialName("active_needs") val activeNeeds: Long = 0,
    @SerialName("verified_orphanages") val verifiedOrphanages: Long = 0,
    @SerialName("pending_orphanages") val pendingOrphanages: Long = 0
)

@Serializable
data class AdminActivityLog(
    val id: String,
    @SerialName("admin_id") val adminId: String,
    @SerialName("action_type") val actionType: String,
    @SerialName("target_type") val targetType: String? = null,
    @SerialName("target_id") val targetId: String? = null,
    val description: String? = null,
    @SerialName("created_at") val createdAt: String
)

data class OrphanageVerificationItem(
    val id: String,
    val orphanageName: String,
    val email: String,
    val city: String,
    val state: String,
    val verificationStatus: String,
    val registrationNumber: String?,
    val createdAt: String?
)


@Serializable
data class OrphanageProfileData(
    val id: String,
    @SerialName("orphanage_name") val orphanage_name: String,
    val city: String,
    val state: String,
    @SerialName("verification_status") val verification_status: String? = "pending",
    @SerialName("registration_number") val registration_number: String? = null,
    @SerialName("created_at") val created_at: String? = null
)
