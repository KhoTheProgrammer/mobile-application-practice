package com.example.myapplication.data.repository

import com.example.myapplication.data.dto.NeedDto
import com.example.myapplication.data.dto.toNeed
import com.example.myapplication.data.model.orphanages.Need
import com.example.myapplication.data.model.orphanages.Orphanage
import com.example.myapplication.data.model.orphanages.Priority
import com.example.myapplication.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class OrphanageDto(
    val id: String,
    @SerialName("orphanage_name")
    val orphanageName: String,
    val description: String? = null,
    val address: String,
    val city: String,
    val state: String,
    val country: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("contact_phone")
    val contactPhone: String? = null,
    @SerialName("contact_email")
    val contactEmail: String? = null,
    val website: String? = null,
    @SerialName("number_of_children")
    val numberOfChildren: Int = 0,
    @SerialName("total_donations_received")
    val totalDonationsReceived: Double = 0.0,
    val rating: Double = 0.0,
    @SerialName("rating_count")
    val ratingCount: Int = 0,
    @SerialName("image_url")
    val imageUrl: String? = null,
    val verified: Boolean = false
)

sealed class OrphanageResult<out T> {
    data class Success<T>(val data: T) : OrphanageResult<T>()
    data class Error(val message: String) : OrphanageResult<Nothing>()
}

class OrphanageRepository {
    private val client = SupabaseClient.client

    /**
     * Get all orphanages
     */
    suspend fun getAllOrphanages(): OrphanageResult<List<Orphanage>> {
        return try {
            val orphanages = client.from("orphanage_profiles")
                .select()
                .decodeList<OrphanageDto>()

            val result = orphanages.map { it.toOrphanage() }
            OrphanageResult.Success(result)
        } catch (e: Exception) {
            OrphanageResult.Error(e.message ?: "Failed to fetch orphanages")
        }
    }

    /**
     * Get orphanage by ID
     */
    suspend fun getOrphanageById(id: String): OrphanageResult<Orphanage> {
        return try {
            val orphanages = client.from("orphanage_profiles")
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeList<OrphanageDto>()

            val orphanage = orphanages.firstOrNull()
                ?: return OrphanageResult.Error("Orphanage not found")

            OrphanageResult.Success(orphanage.toOrphanage())
        } catch (e: Exception) {
            OrphanageResult.Error(e.message ?: "Failed to fetch orphanage")
        }
    }

    /**
     * Search orphanages by name or location
     */
    suspend fun searchOrphanages(query: String): OrphanageResult<List<Orphanage>> {
        return try {
            val orphanages = client.from("orphanage_profiles")
                .select {
                    filter {
                        or {
                            ilike("orphanage_name", "%$query%")
                            ilike("city", "%$query%")
                            ilike("state", "%$query%")
                        }
                    }
                }
                .decodeList<OrphanageDto>()

            val result = orphanages.map { it.toOrphanage() }
            OrphanageResult.Success(result)
        } catch (e: Exception) {
            OrphanageResult.Error(e.message ?: "Search failed")
        }
    }

    /**
     * Get orphanages by city
     */
    suspend fun getOrphanagesByCity(city: String): OrphanageResult<List<Orphanage>> {
        return try {
            val orphanages = client.from("orphanage_profiles")
                .select {
                    filter {
                        eq("city", city)
                    }
                }
                .decodeList<OrphanageDto>()

            val result = orphanages.map { it.toOrphanage() }
            OrphanageResult.Success(result)
        } catch (e: Exception) {
            OrphanageResult.Error(e.message ?: "Failed to fetch orphanages")
        }
    }

    /**
     * Get verified orphanages
     */
    suspend fun getVerifiedOrphanages(): OrphanageResult<List<Orphanage>> {
        return try {
            val orphanages = client.from("orphanage_profiles")
                .select {
                    filter {
                        eq("verified", true)
                    }
                }
                .decodeList<OrphanageDto>()

            val result = orphanages.map { it.toOrphanage() }
            OrphanageResult.Success(result)
        } catch (e: Exception) {
            OrphanageResult.Error(e.message ?: "Failed to fetch verified orphanages")
        }
    }

    /**
     * Get orphanage needs
     */
    suspend fun getOrphanageNeeds(orphanageId: String): OrphanageResult<List<Need>> {
        return try {
            val needs = client.from("needs")
                .select {
                    filter {
                        eq("orphanage_id", orphanageId)
                        eq("status", "active")
                    }
                }
                .decodeList<NeedDto>()

            val result = needs.map { it.toNeed() }
            OrphanageResult.Success(result)
        } catch (e: Exception) {
            OrphanageResult.Error(e.message ?: "Failed to fetch needs")
        }
    }

    /**
     * Create a new need for an orphanage
     */
    suspend fun createNeed(
        orphanageId: String,
        categoryId: String,
        itemName: String,
        quantity: Int,
        priority: Priority,
        description: String
    ): OrphanageResult<Need> {
        return try {
            val needData = mapOf(
                "orphanage_id" to orphanageId,
                "category_id" to categoryId,
                "item_name" to itemName,
                "quantity" to quantity,
                "priority" to priority.name,
                "description" to description,
                "status" to "active"
            )

            client.from("needs").insert(needData)

            // Fetch the created need
            val needs = client.from("needs")
                .select {
                    filter {
                        eq("orphanage_id", orphanageId)
                        eq("item_name", itemName)
                    }
                }
                .decodeList<NeedDto>()

            val need = needs.firstOrNull()
                ?: return OrphanageResult.Error("Failed to create need")

            OrphanageResult.Success(need.toNeed())
        } catch (e: Exception) {
            OrphanageResult.Error(e.message ?: "Failed to create need")
        }
    }

    /**
     * Update a need
     */
    suspend fun updateNeed(
        needId: String,
        itemName: String? = null,
        quantity: Int? = null,
        priority: Priority? = null,
        description: String? = null
    ): OrphanageResult<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>()
            itemName?.let { updates["item_name"] = it }
            quantity?.let { updates["quantity"] = it }
            priority?.let { updates["priority"] = it.name }
            description?.let { updates["description"] = it }

            if (updates.isNotEmpty()) {
                client.from("needs").update(updates) {
                    filter {
                        eq("id", needId)
                    }
                }
            }

            OrphanageResult.Success(Unit)
        } catch (e: Exception) {
            OrphanageResult.Error(e.message ?: "Failed to update need")
        }
    }

    /**
     * Delete a need
     */
    suspend fun deleteNeed(needId: String): OrphanageResult<Unit> {
        return try {
            client.from("needs").delete {
                filter {
                    eq("id", needId)
                }
            }
            OrphanageResult.Success(Unit)
        } catch (e: Exception) {
            OrphanageResult.Error(e.message ?: "Failed to delete need")
        }
    }

    /**
     * Update orphanage profile
     */
    suspend fun updateOrphanageProfile(
        orphanageId: String,
        orphanageName: String? = null,
        description: String? = null,
        address: String? = null,
        city: String? = null,
        state: String? = null,
        contactPhone: String? = null,
        contactEmail: String? = null,
        website: String? = null,
        numberOfChildren: Int? = null,
        imageUrl: String? = null
    ): OrphanageResult<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>()
            orphanageName?.let { updates["orphanage_name"] = it }
            description?.let { updates["description"] = it }
            address?.let { updates["address"] = it }
            city?.let { updates["city"] = it }
            state?.let { updates["state"] = it }
            contactPhone?.let { updates["contact_phone"] = it }
            contactEmail?.let { updates["contact_email"] = it }
            website?.let { updates["website"] = it }
            numberOfChildren?.let { updates["number_of_children"] = it }
            imageUrl?.let { updates["image_url"] = it }

            if (updates.isNotEmpty()) {
                client.from("orphanage_profiles").update(updates) {
                    filter {
                        eq("id", orphanageId)
                    }
                }
            }

            OrphanageResult.Success(Unit)
        } catch (e: Exception) {
            OrphanageResult.Error(e.message ?: "Failed to update profile")
        }
    }

    /**
     * Get orphanages sorted by rating
     */
    suspend fun getTopRatedOrphanages(limit: Int = 10): OrphanageResult<List<Orphanage>> {
        return try {
            val orphanages = client.from("orphanage_profiles")
                .select {
                    filter {
                        gte("rating", 4.0)
                    }
                    order(column = "rating", order = Order.DESCENDING)
                    limit(count = limit.toLong())
                }
                .decodeList<OrphanageDto>()

            val result = orphanages.map { it.toOrphanage() }
            OrphanageResult.Success(result)
        } catch (e: Exception) {
            OrphanageResult.Error(e.message ?: "Failed to fetch top rated orphanages")
        }
    }

    /**
     * Get orphanages with urgent needs
     */
    suspend fun getOrphanagesWithUrgentNeeds(): OrphanageResult<List<Orphanage>> {
        return try {
            // Get orphanage IDs with urgent needs
            val urgentNeeds = client.from("needs")
                .select {
                    filter {
                        eq("priority", "URGENT")
                        eq("status", "active")
                    }
                }
                .decodeList<NeedDto>()

            val orphanageIds = urgentNeeds.map { it.orphanageId }.distinct()

            if (orphanageIds.isEmpty()) {
                return OrphanageResult.Success(emptyList())
            }

            val orphanages = client.from("orphanage_profiles")
                .select {
                    filter {
                        isIn("id", orphanageIds)
                    }
                }
                .decodeList<OrphanageDto>()

            val result = orphanages.map { it.toOrphanage() }
            OrphanageResult.Success(result)
        } catch (e: Exception) {
            OrphanageResult.Error(e.message ?: "Failed to fetch orphanages with urgent needs")
        }
    }

    // Extension functions to convert DTOs to domain models
    private fun OrphanageDto.toOrphanage(): Orphanage {
        return Orphanage(
            id = id,
            name = orphanageName,
            description = description ?: "",
            address = address,
            distance = "0 km", // Calculate distance based on user location
            needs = "", // Will be populated from needs table
            rating = rating.toFloat(),
            imageUrl = imageUrl,
            contactInfo = com.example.myapplication.data.model.orphanages.ContactInfo(
                phone = contactPhone ?: "",
                email = contactEmail ?: "",
                website = website ?: ""
            ),
            currentNeeds = emptyList(), // Will be populated separately
            totalDonationsReceived = totalDonationsReceived,
            numberOfChildren = numberOfChildren
        )
    }


}
