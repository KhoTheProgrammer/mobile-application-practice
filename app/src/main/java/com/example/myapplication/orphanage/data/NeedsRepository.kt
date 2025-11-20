package com.example.myapplication.orphanage.data

import android.util.Log
import com.example.myapplication.core.data.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.Serializable

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

    private suspend fun getCategoryIdByName(categoryName: String): String? {
        return try {
            @Serializable
            data class CategoryDto(val id: String, val name: String)
            
            val categories = client.from("categories")
                .select {
                    filter {
                        eq("name", categoryName)
                    }
                }
                .decodeList<CategoryDto>()
            categories.firstOrNull()?.id
        } catch (e: Exception) {
            Log.e("NeedsRepository", "Failed to get category ID: ${e.message}")
            null
        }
    }

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
            // Get the category UUID from the category name
            val categoryUuid = getCategoryIdByName(categoryId)
            if (categoryUuid == null) {
                return NeedsResult.Error("Invalid category: $categoryId")
            }
            
            val needInsert = NeedInsertDto(
                orphanageId = orphanageId,
                categoryId = categoryUuid,
                itemName = itemName,
                quantity = quantity,
                priority = priority.name,
                description = description,
                status = "active"
            )
            
            Log.d("NeedsRepository", "Creating need: $needInsert")
            client.from("needs").insert(needInsert)
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
