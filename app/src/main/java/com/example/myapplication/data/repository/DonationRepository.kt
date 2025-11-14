package com.example.myapplication.data.repository

import com.example.myapplication.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.Serializable

@Serializable
data class DonationDto(
    val id: String,
    val donor_id: String,
    val orphanage_id: String,
    val category_id: String,
    val need_id: String? = null,
    val amount: Double,
    val currency: String = "USD",
    val donation_type: String = "monetary",
    val item_description: String? = null,
    val quantity: Int? = null,
    val status: String = "pending",
    val note: String? = null,
    val is_anonymous: Boolean = false,
    val is_recurring: Boolean = false,
    val recurring_frequency: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val completed_at: String? = null
)

@Serializable
data class DonationWithDetailsDto(
    val id: String,
    val donor_id: String,
    val orphanage_id: String,
    val category_id: String,
    val amount: Double,
    val currency: String = "USD",
    val donation_type: String,
    val status: String,
    val created_at: String? = null,
    val orphanage_name: String? = null,
    val category_name: String? = null
)

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

class DonationRepository {
    private val client = SupabaseClient.client

    /**
     * Create a new donation
     */
    suspend fun createDonation(
        donorId: String,
        orphanageId: String,
        categoryId: String,
        amount: Double,
        donationType: DonationType = DonationType.MONETARY,
        needId: String? = null,
        itemDescription: String? = null,
        quantity: Int? = null,
        note: String? = null,
        isAnonymous: Boolean = false,
        isRecurring: Boolean = false,
        recurringFrequency: RecurringFrequency? = null
    ): DonationResult<Donation> {
        return try {
            val donationData = mutableMapOf<String, Any>(
                "donor_id" to donorId,
                "orphanage_id" to orphanageId,
                "category_id" to categoryId,
                "amount" to amount,
                "donation_type" to donationType.name.lowercase(),
                "status" to "pending",
                "is_anonymous" to isAnonymous,
                "is_recurring" to isRecurring
            )

            needId?.let { donationData["need_id"] = it }
            itemDescription?.let { donationData["item_description"] = it }
            quantity?.let { donationData["quantity"] = it }
            note?.let { donationData["note"] = it }
            recurringFrequency?.let { donationData["recurring_frequency"] = it.name.lowercase() }

            client.from("donations").insert(donationData)

            // Fetch the created donation
            val donations = client.from("donations")
                .select {
                    filter {
                        eq("donor_id", donorId)
                        eq("orphanage_id", orphanageId)
                    }
                    order(column = "created_at", order = Order.DESCENDING)
                    limit(count = 1)
                }
                .decodeList<DonationDto>()

            val donation = donations.firstOrNull()
                ?: return DonationResult.Error("Failed to create donation")

            DonationResult.Success(donation.toDonation())
        } catch (e: Exception) {
            DonationResult.Error(e.message ?: "Failed to create donation")
        }
    }

    /**
     * Get donations by donor
     */
    suspend fun getDonationsByDonor(donorId: String): DonationResult<List<Donation>> {
        return try {
            val donations = client.from("donations")
                .select {
                    filter {
                        eq("donor_id", donorId)
                    }
                    order(column = "created_at", order = Order.DESCENDING)
                }
                .decodeList<DonationDto>()

            val result = donations.map { it.toDonation() }
            DonationResult.Success(result)
        } catch (e: Exception) {
            DonationResult.Error(e.message ?: "Failed to fetch donations")
        }
    }

    /**
     * Get donations by orphanage
     */
    suspend fun getDonationsByOrphanage(orphanageId: String): DonationResult<List<Donation>> {
        return try {
            val donations = client.from("donations")
                .select {
                    filter {
                        eq("orphanage_id", orphanageId)
                    }
                    order(column = "created_at", order = Order.DESCENDING)
                }
                .decodeList<DonationDto>()

            val result = donations.map { it.toDonation() }
            DonationResult.Success(result)
        } catch (e: Exception) {
            DonationResult.Error(e.message ?: "Failed to fetch donations")
        }
    }

    /**
     * Get donation by ID
     */
    suspend fun getDonationById(donationId: String): DonationResult<Donation> {
        return try {
            val donations = client.from("donations")
                .select {
                    filter {
                        eq("id", donationId)
                    }
                }
                .decodeList<DonationDto>()

            val donation = donations.firstOrNull()
                ?: return DonationResult.Error("Donation not found")

            DonationResult.Success(donation.toDonation())
        } catch (e: Exception) {
            DonationResult.Error(e.message ?: "Failed to fetch donation")
        }
    }

    /**
     * Get donations by status
     */
    suspend fun getDonationsByStatus(
        status: DonationStatus,
        donorId: String? = null,
        orphanageId: String? = null
    ): DonationResult<List<Donation>> {
        return try {
            val donations = client.from("donations")
                .select {
                    filter {
                        eq("status", status.name.lowercase())
                        donorId?.let { eq("donor_id", it) }
                        orphanageId?.let { eq("orphanage_id", it) }
                    }
                    order(column = "created_at", order = Order.DESCENDING)
                }
                .decodeList<DonationDto>()

            val result = donations.map { it.toDonation() }
            DonationResult.Success(result)
        } catch (e: Exception) {
            DonationResult.Error(e.message ?: "Failed to fetch donations by status")
        }
    }

    /**
     * Get pending donations for a donor
     */
    suspend fun getPendingDonations(donorId: String): DonationResult<List<Donation>> {
        return getDonationsByStatus(DonationStatus.PENDING, donorId = donorId)
    }

    /**
     * Get completed donations for a donor
     */
    suspend fun getCompletedDonations(donorId: String): DonationResult<List<Donation>> {
        return getDonationsByStatus(DonationStatus.COMPLETED, donorId = donorId)
    }

    /**
     * Update donation status
     */
    suspend fun updateDonationStatus(
        donationId: String,
        status: DonationStatus
    ): DonationResult<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "status" to status.name.lowercase()
            )

            if (status == DonationStatus.COMPLETED) {
                updates["completed_at"] = "now()"
            }

            client.from("donations").update(updates) {
                filter {
                    eq("id", donationId)
                }
            }

            DonationResult.Success(Unit)
        } catch (e: Exception) {
            DonationResult.Error(e.message ?: "Failed to update donation status")
        }
    }

    /**
     * Confirm a donation
     */
    suspend fun confirmDonation(donationId: String): DonationResult<Unit> {
        return updateDonationStatus(donationId, DonationStatus.CONFIRMED)
    }

    /**
     * Complete a donation
     */
    suspend fun completeDonation(donationId: String): DonationResult<Unit> {
        return updateDonationStatus(donationId, DonationStatus.COMPLETED)
    }

    /**
     * Cancel a donation
     */
    suspend fun cancelDonation(donationId: String): DonationResult<Unit> {
        return updateDonationStatus(donationId, DonationStatus.CANCELLED)
    }

    /**
     * Get donation statistics for a donor
     */
    suspend fun getDonorStatistics(donorId: String): DonationResult<DonationStatistics> {
        return try {
            val donations = client.from("donations")
                .select {
                    filter {
                        eq("donor_id", donorId)
                    }
                }
                .decodeList<DonationDto>()

            val totalAmount = donations
                .filter { it.status == "completed" }
                .sumOf { it.amount }

            val statistics = DonationStatistics(
                totalDonations = donations.size,
                totalAmount = totalAmount,
                pendingDonations = donations.count { it.status == "pending" },
                completedDonations = donations.count { it.status == "completed" },
                monetaryDonations = donations.count { it.donation_type == "monetary" },
                inKindDonations = donations.count { it.donation_type == "in_kind" }
            )

            DonationResult.Success(statistics)
        } catch (e: Exception) {
            DonationResult.Error(e.message ?: "Failed to fetch statistics")
        }
    }

    /**
     * Get donation statistics for an orphanage
     */
    suspend fun getOrphanageStatistics(orphanageId: String): DonationResult<DonationStatistics> {
        return try {
            val donations = client.from("donations")
                .select {
                    filter {
                        eq("orphanage_id", orphanageId)
                    }
                }
                .decodeList<DonationDto>()

            val totalAmount = donations
                .filter { it.status == "completed" }
                .sumOf { it.amount }

            val statistics = DonationStatistics(
                totalDonations = donations.size,
                totalAmount = totalAmount,
                pendingDonations = donations.count { it.status == "pending" },
                completedDonations = donations.count { it.status == "completed" },
                monetaryDonations = donations.count { it.donation_type == "monetary" },
                inKindDonations = donations.count { it.donation_type == "in_kind" }
            )

            DonationResult.Success(statistics)
        } catch (e: Exception) {
            DonationResult.Error(e.message ?: "Failed to fetch statistics")
        }
    }

    /**
     * Get recent donations (last 30 days)
     */
    suspend fun getRecentDonations(
        donorId: String? = null,
        orphanageId: String? = null,
        limit: Int = 10
    ): DonationResult<List<Donation>> {
        return try {
            val donations = client.from("donations")
                .select {
                    filter {
                        donorId?.let { eq("donor_id", it) }
                        orphanageId?.let { eq("orphanage_id", it) }
                    }
                    order(column = "created_at", order = Order.DESCENDING)
                    limit(count = limit.toLong())
                }
                .decodeList<DonationDto>()

            val result = donations.map { it.toDonation() }
            DonationResult.Success(result)
        } catch (e: Exception) {
            DonationResult.Error(e.message ?: "Failed to fetch recent donations")
        }
    }

    /**
     * Get recurring donations for a donor
     */
    suspend fun getRecurringDonations(donorId: String): DonationResult<List<Donation>> {
        return try {
            val donations = client.from("donations")
                .select {
                    filter {
                        eq("donor_id", donorId)
                        eq("is_recurring", true)
                        eq("status", "completed")
                    }
                    order(column = "created_at", order = Order.DESCENDING)
                }
                .decodeList<DonationDto>()

            val result = donations.map { it.toDonation() }
            DonationResult.Success(result)
        } catch (e: Exception) {
            DonationResult.Error(e.message ?: "Failed to fetch recurring donations")
        }
    }

    /**
     * Get donations by category
     */
    suspend fun getDonationsByCategory(
        categoryId: String,
        donorId: String? = null,
        orphanageId: String? = null
    ): DonationResult<List<Donation>> {
        return try {
            val donations = client.from("donations")
                .select {
                    filter {
                        eq("category_id", categoryId)
                        donorId?.let { eq("donor_id", it) }
                        orphanageId?.let { eq("orphanage_id", it) }
                    }
                    order(column = "created_at", order = Order.DESCENDING)
                }
                .decodeList<DonationDto>()

            val result = donations.map { it.toDonation() }
            DonationResult.Success(result)
        } catch (e: Exception) {
            DonationResult.Error(e.message ?: "Failed to fetch donations by category")
        }
    }

    /**
     * Get top donors for an orphanage
     */
    suspend fun getTopDonors(orphanageId: String, limit: Int = 10): DonationResult<List<DonorSummary>> {
        return try {
            val donations = client.from("donations")
                .select {
                    filter {
                        eq("orphanage_id", orphanageId)
                        eq("status", "completed")
                    }
                }
                .decodeList<DonationDto>()

            // Group by donor and sum amounts
            val donorMap = donations
                .groupBy { it.donor_id }
                .mapValues { (_, donations) ->
                    DonorSummary(
                        donorId = donations.first().donor_id,
                        totalAmount = donations.sumOf { it.amount },
                        donationCount = donations.size
                    )
                }
                .values
                .sortedByDescending { it.totalAmount }
                .take(limit)

            DonationResult.Success(donorMap)
        } catch (e: Exception) {
            DonationResult.Error(e.message ?: "Failed to fetch top donors")
        }
    }

    /**
     * Delete a donation (only if pending)
     */
    suspend fun deleteDonation(donationId: String): DonationResult<Unit> {
        return try {
            // Check if donation is pending
            val donationResult = getDonationById(donationId)
            if (donationResult is DonationResult.Error) {
                return DonationResult.Error(donationResult.message)
            }

            val donation = (donationResult as DonationResult.Success).data
            if (donation.status != DonationStatus.PENDING) {
                return DonationResult.Error("Can only delete pending donations")
            }

            client.from("donations").delete {
                filter {
                    eq("id", donationId)
                }
            }

            DonationResult.Success(Unit)
        } catch (e: Exception) {
            DonationResult.Error(e.message ?: "Failed to delete donation")
        }
    }

    // Extension function to convert DTO to domain model
    private fun DonationDto.toDonation(): Donation {
        return Donation(
            id = id,
            donorId = donor_id,
            orphanageId = orphanage_id,
            categoryId = category_id,
            needId = need_id,
            amount = amount,
            currency = currency,
            donationType = when (donation_type) {
                "monetary" -> DonationType.MONETARY
                "in_kind" -> DonationType.IN_KIND
                else -> DonationType.MONETARY
            },
            itemDescription = item_description,
            quantity = quantity,
            status = when (status) {
                "pending" -> DonationStatus.PENDING
                "confirmed" -> DonationStatus.CONFIRMED
                "completed" -> DonationStatus.COMPLETED
                "cancelled" -> DonationStatus.CANCELLED
                else -> DonationStatus.PENDING
            },
            note = note,
            isAnonymous = is_anonymous,
            isRecurring = is_recurring,
            recurringFrequency = recurring_frequency?.let {
                when (it) {
                    "weekly" -> RecurringFrequency.WEEKLY
                    "monthly" -> RecurringFrequency.MONTHLY
                    "quarterly" -> RecurringFrequency.QUARTERLY
                    "yearly" -> RecurringFrequency.YEARLY
                    else -> null
                }
            },
            createdAt = created_at,
            completedAt = completed_at
        )
    }
}

// Supporting data classes
data class DonorSummary(
    val donorId: String,
    val totalAmount: Double,
    val donationCount: Int
)
