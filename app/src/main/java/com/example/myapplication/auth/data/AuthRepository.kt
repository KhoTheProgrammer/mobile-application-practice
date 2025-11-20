package com.example.myapplication.auth.data

import com.example.myapplication.core.data.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository {
    private val client = SupabaseClient.client

    suspend fun signUp(
        email: String,
        password: String,
        fullName: String,
        userType: UserType
    ): AuthResult {
        return try {
            // Sign up with Supabase Auth
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            // Get user ID from current session
            val userId = client.auth.currentUserOrNull()?.id
                ?: throw Exception("User ID not found. Please check if email confirmation is required.")

            // Create profile
            client.from("profiles").insert(
                mapOf(
                    "id" to userId,
                    "user_type" to userType.name.lowercase(),
                    "full_name" to fullName,
                    "email" to email
                )
            )

            // Create user-type specific profile
            when (userType) {
                UserType.DONOR -> {
                    client.from("donor_profiles").insert(
                        mapOf("id" to userId)
                    )
                }
                UserType.ORPHANAGE -> {
                    // Create orphanage profile with minimal required fields
                    client.from("orphanage_profiles").insert(
                        mapOf(
                            "id" to userId,
                            "orphanage_name" to fullName,
                            "address" to "To be updated",
                            "city" to "To be updated",
                            "state" to "To be updated",
                            "country" to "Malawi"
                        )
                    )
                }
                UserType.ADMIN -> {
                    // No admin-specific profile to create
                }
            }

            AuthResult.Success(
                User(
                    id = userId,
                    email = email,
                    userType = userType,
                    fullName = fullName
                )
            )
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Sign up failed")
        }
    }

    suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            // Sign in with Supabase Auth
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val userId = client.auth.currentUserOrNull()?.id
                ?: throw Exception("User not found after sign in")

            // Fetch user profile
            val profiles = client.from("profiles")
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeList<Profile>()

            val profile = profiles.firstOrNull()
                ?: throw Exception("Profile not found. Please sign up first.")

            AuthResult.Success(
                User(
                    id = profile.id,
                    email = profile.email,
                    userType = when (profile.user_type) {
                        "donor" -> UserType.DONOR
                        "orphanage" -> UserType.ORPHANAGE
                        "admin" -> UserType.ADMIN
                        else -> throw Exception("Invalid user type")
                    },
                    fullName = profile.full_name,
                    phone = profile.phone,
                    avatarUrl = profile.avatar_url,
                    createdAt = profile.created_at
                )
            )
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Sign in failed")
        }
    }

    suspend fun signOut(): Result<Unit> {
        return try {
            client.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): User? {
        return try {
            val userId = client.auth.currentUserOrNull()?.id ?: return null

            val profiles = client.from("profiles")
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeList<Profile>()

            val profile = profiles.firstOrNull() ?: return null

            User(
                id = profile.id,
                email = profile.email,
                userType = when (profile.user_type) {
                    "donor" -> UserType.DONOR
                    "orphanage" -> UserType.ORPHANAGE
                    "admin" -> UserType.ADMIN
                    else -> UserType.DONOR
                },
                fullName = profile.full_name,
                phone = profile.phone,
                avatarUrl = profile.avatar_url,
                createdAt = profile.created_at
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateProfile(
        fullName: String? = null,
        phone: String? = null,
        avatarUrl: String? = null
    ): Result<Unit> {
        return try {
            val userId = client.auth.currentUserOrNull()?.id
                ?: throw Exception("User not logged in")

            android.util.Log.d("AuthRepository", "Updating profile for user: $userId")
            android.util.Log.d("AuthRepository", "fullName: $fullName, phone: $phone")

            // Build update map with only non-null values
            val updates = buildMap {
                fullName?.let { put("full_name", it) }
                phone?.let { put("phone", it) }
                avatarUrl?.let { put("avatar_url", it) }
            }

            if (updates.isNotEmpty()) {
                android.util.Log.d("AuthRepository", "Updates to apply: $updates")

                client.from("profiles").update(updates) {
                    filter {
                        eq("id", userId)
                    }
                }

                android.util.Log.d("AuthRepository", "Profile updated successfully")
            } else {
                android.util.Log.d("AuthRepository", "No updates to apply")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Failed to update profile", e)
            Result.failure(e)
        }
    }
}