package com.example.myapplication.data.repository

import com.example.myapplication.data.dto.NeedDto
import com.example.myapplication.data.dto.toNeed
import com.example.myapplication.data.model.orphanages.Need
import com.example.myapplication.data.model.orphanages.Priority
import com.example.myapplication.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

sealed class NeedsResult<out T> {
    data class Success<T>(val data: T) : NeedsResult<T>()
    data class Error(val message: String) : NeedsResult<Nothing>()
}

data class NeedsStatistics(
    val totalNeeds: Int,
    val activeNeeds: Int,
    val fulfilledNeeds: Int,
    val cancelledNeeds: Int,
    val urgentNeeds: Int,
    val highPriorityNeeds: Int
)

data class NeedCreateRequest(
    val orphanageId: String,
    val categoryId: String,
    val itemName: String,
    val quantity: Int,
    val priority: Priority,
    val description: String
)

class NeedsRepository {
    private val client = SupabaseClient.client

    suspend fun getAllActiveNeeds(): NeedsResult<List<Need>> {
        return try {
            val needs = client.from("needs")
                .select {
                    filter {
                        eq("status", "active")
                    }
                    order(column = "created_at", order = Order.DESCENDING)
                }
                .decodeList<NeedDto>()
            NeedsResult.Success(needs.map { it.toNeed() })
        } catch (e: Exception) {
            NeedsResult.Error(e.message ?: "Failed to fetch needs")
        }
    }

    suspend fun getNeedsByOrphanage(orphanageId: String): NeedsResult<List<Need>> {
        return try {
            val needs = client.from("needs")
                .select {
                    filter {
                        eq("orphanage_id", orphanageId)
                        eq("status", "active")
                    }
                    order(column = "priority", order = Order.DESCENDING)
                }
                .decodeList<NeedDto>()
            NeedsResult.Success(needs.map { it.toNeed() })
        } catch (e: Exception) {
            NeedsResult.Error(e.message ?: "Failed to fetch needs")
        }
    }

    suspend fun getNeedsByCategory(categoryId: String): NeedsResult<List<Need>> {
        return try {
            val needs = client.from("needs")
                .select {
                    filter {
                        eq("category_id", categoryId)
                        eq("status", "active")
                    }
                }
                .decodeList<NeedDto>()
            NeedsResult.Success(needs.map { it.toNeed() })
        } catch (e: Exception) {
            NeedsResult.Error(e.message ?: "Failed to fetch needs")
        }
    }

    suspend fun getNeedById(needId: String): NeedsResult<Need> {
        return try {
            val needs = client.from("needs")
                .select {
                    filter {
                        eq("id", needId)
                    }
                }
                .decodeList<NeedDto>()
            val need = needs.firstOrNull() ?: return NeedsResult.Error("Need not found")
            NeedsResult.Success(need.toNeed())
        } catch (e: Exception) {
            NeedsResult.Error(e.message ?: "Failed to fetch need")
        }
    }

    suspend fun createNeed(
        orphanageId: String,
        categoryId: String,
        itemName: String,
        quantity: Int,
        priority: Priority,
        description: String
    ): NeedsResult<Need> {
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
            val needs = client.from("needs")
                .select {
                    filter {
                        eq("orphanage_id", orphanageId)
                        eq("item_name", itemName)
                    }
                    order(column = "created_at", order = Order.DESCENDING)
                    limit(count = 1)
                }
                .decodeList<NeedDto>()
            val need = needs.firstOrNull() ?: return NeedsResult.Error("Failed to create need")
            NeedsResult.Success(need.toNeed())
        } catch (e: Exception) {
            NeedsResult.Error(e.message ?: "Failed to create need")
        }
    }

    suspend fun updateNeed(
        needId: String,
        itemName: String? = null,
        quantity: Int? = null,
        priority: Priority? = null,
        description: String? = null
    ): NeedsResult<Unit> {
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
            NeedsResult.Success(Unit)
        } catch (e: Exception) {
            NeedsResult.Error(e.message ?: "Failed to update need")
        }
    }

    suspend fun deleteNeed(needId: String): NeedsResult<Unit> {
        return try {
            client.from("needs").delete {
                filter {
                    eq("id", needId)
                }
            }
            NeedsResult.Success(Unit)
        } catch (e: Exception) {
            NeedsResult.Error(e.message ?: "Failed to delete need")
        }
    }

    suspend fun markNeedAsFulfilled(needId: String): NeedsResult<Unit> {
        return try {
            client.from("needs").update(mapOf("status" to "fulfilled")) {
                filter {
                    eq("id", needId)
                }
            }
            NeedsResult.Success(Unit)
        } catch (e: Exception) {
            NeedsResult.Error(e.message ?: "Failed to mark need as fulfilled")
        }
    }

    suspend fun getNeedsStatistics(orphanageId: String): NeedsResult<NeedsStatistics> {
        return try {
            val allNeeds = client.from("needs")
                .select {
                    filter {
                        eq("orphanage_id", orphanageId)
                    }
                }
                .decodeList<NeedDto>()
            val statistics = NeedsStatistics(
                totalNeeds = allNeeds.size,
                activeNeeds = allNeeds.count { it.status == "active" },
                fulfilledNeeds = allNeeds.count { it.status == "fulfilled" },
                cancelledNeeds = allNeeds.count { it.status == "cancelled" },
                urgentNeeds = allNeeds.count { it.priority == "URGENT" && it.status == "active" },
                highPriorityNeeds = allNeeds.count { it.priority == "HIGH" && it.status == "active" }
            )
            NeedsResult.Success(statistics)
        } catch (e: Exception) {
            NeedsResult.Error(e.message ?: "Failed to fetch statistics")
        }
    }


}
