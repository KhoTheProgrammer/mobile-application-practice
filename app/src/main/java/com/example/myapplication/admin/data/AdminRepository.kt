package com.example.myapplication.admin.data

import com.example.myapplication.core.data.SupabaseClient
import io.github.jan.supabase.postgrest.from

class AdminRepository {
    private val client = SupabaseClient.client

    suspend fun getDashboardStats(): Result<DashboardStats> {
        return try {
            // Return default stats for now
            Result.success(DashboardStats())
        } catch (e: Exception) {
            android.util.Log.e("AdminRepository", "Error fetching dashboard stats", e)
            Result.failure(e)
        }
    }

    suspend fun getAllUsers(): Result<List<UserManagementItem>> {
        return try {
            val users = client.from("profiles")
                .select()
                .decodeList<UserManagementItem>()
            
            Result.success(users)
        } catch (e: Exception) {
            android.util.Log.e("AdminRepository", "Error fetching users", e)
            Result.failure(e)
        }
    }

    suspend fun updateUserStatus(userId: String, status: String): Result<Unit> {
        return try {
            client.from("profiles").update(
                mapOf("status" to status)
            ) {
                filter {
                    eq("id", userId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("AdminRepository", "Error updating user status", e)
            Result.failure(e)
        }
    }

    suspend fun verifyUser(userId: String, verified: Boolean): Result<Unit> {
        return try {
            client.from("profiles").update(
                mapOf("verified" to verified)
            ) {
                filter {
                    eq("id", userId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("AdminRepository", "Error verifying user", e)
            Result.failure(e)
        }
    }

    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            client.from("profiles").delete {
                filter {
                    eq("id", userId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("AdminRepository", "Error deleting user", e)
            Result.failure(e)
        }
    }

    suspend fun getPendingOrphanageVerifications(): Result<List<OrphanageVerificationItem>> {
        return try {
            val orphanages = client.from("orphanage_profiles")
                .select()
                .decodeList<OrphanageProfileData>()
                .filter { it.verification_status == "pending" }
            
            val items = orphanages.mapNotNull { orphanage ->
                try {
                    val profiles = client.from("profiles")
                        .select {
                            filter {
                                eq("id", orphanage.id)
                            }
                        }
                        .decodeList<UserManagementItem>()
                    
                    val profile = profiles.firstOrNull() ?: return@mapNotNull null
                    
                    OrphanageVerificationItem(
                        id = orphanage.id,
                        orphanageName = orphanage.orphanage_name,
                        email = profile.email,
                        city = orphanage.city,
                        state = orphanage.state,
                        verificationStatus = orphanage.verification_status ?: "pending",
                        registrationNumber = orphanage.registration_number,
                        createdAt = orphanage.created_at
                    )
                } catch (e: Exception) {
                    null
                }
            }
            
            Result.success(items)
        } catch (e: Exception) {
            android.util.Log.e("AdminRepository", "Error fetching pending verifications", e)
            Result.failure(e)
        }
    }

    suspend fun verifyOrphanage(
        orphanageId: String,
        adminId: String,
        status: String,
        notes: String? = null
    ): Result<Unit> {
        return try {
            val updates = buildMap {
                put("verification_status", status)
                put("verified_by", adminId)
                put("verified_at", java.time.Instant.now().toString())
                if (notes != null) put("verification_notes", notes)
                if (status == "verified") put("verified", true)
            }
            
            client.from("orphanage_profiles").update(updates) {
                filter {
                    eq("id", orphanageId)
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("AdminRepository", "Error verifying orphanage", e)
            Result.failure(e)
        }
    }

    suspend fun logActivity(
        adminId: String,
        actionType: String,
        targetType: String? = null,
        targetId: String? = null,
        description: String? = null
    ): Result<Unit> {
        return try {
            client.from("admin_activity_logs").insert(
                buildMap {
                    put("admin_id", adminId)
                    put("action_type", actionType)
                    if (targetType != null) put("target_type", targetType)
                    if (targetId != null) put("target_id", targetId)
                    if (description != null) put("description", description)
                }
            )
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("AdminRepository", "Error logging activity", e)
            Result.failure(e)
        }
    }

    suspend fun getRecentActivities(limit: Int = 20): Result<List<AdminActivityLog>> {
        return try {
            val activities = client.from("admin_activity_logs")
                .select()
                .decodeList<AdminActivityLog>()
                .take(limit)
            
            Result.success(activities)
        } catch (e: Exception) {
            android.util.Log.e("AdminRepository", "Error fetching activities", e)
            Result.failure(e)
        }
    }
}
