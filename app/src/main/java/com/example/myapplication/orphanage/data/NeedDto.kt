package com.example.myapplication.orphanage.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A data transfer object for a need.
 *
 * @property id The ID of the need.
 * @property orphanageId The ID of the orphanage that has the need.
 * @property categoryId The ID of the category of the need.
 * @property itemName The name of the item that is needed.
 * @property quantity The quantity of the item that is needed.
 * @property quantityFulfilled The quantity of the item that has been fulfilled.
 * @property priority The priority of the need.
 * @property description A description of the need.
 * @property status The status of the need.
 * @property createdAt The timestamp of when the need was created.
 */
@Serializable
data class NeedDto(
    val id: String,
    @SerialName("orphanage_id")
    val orphanageId: String,
    @SerialName("category_id")
    val categoryId: String,
    @SerialName("item_name")
    val itemName: String,
    val quantity: Int,
    @SerialName("quantity_fulfilled")
    val quantityFulfilled: Int = 0,
    val priority: String,
    val description: String? = null,
    val status: String = "active",
    @SerialName("created_at")
    val createdAt: String
)

/**
 * A data transfer object for inserting a new need.
 *
 * @property orphanageId The ID of the orphanage that has the need.
 * @property categoryId The ID of the category of the need.
 * @property itemName The name of the item that is needed.
 * @property quantity The quantity of the item that is needed.
 * @property priority The priority of the need.
 * @property description A description of the need.
 * @property status The status of the need.
 */
@Serializable
data class NeedInsertDto(
    @SerialName("orphanage_id")
    val orphanageId: String,
    @SerialName("category_id")
    val categoryId: String,  // This should be a UUID from categories table
    @SerialName("item_name")
    val itemName: String,
    val quantity: Int,
    val priority: String,
    val description: String,
    val status: String = "active"
)

/**
 * Converts a [NeedDto] to a [Need].
 *
 * @return A [Need] object.
 */
fun NeedDto.toNeed(): Need {
    return Need(
        id = id,
        category = categoryId,
        item = itemName,
        quantity = quantity,
        priority = Priority.valueOf(priority),
        description = description ?: "",
        createdAt = createdAt
    )
}
