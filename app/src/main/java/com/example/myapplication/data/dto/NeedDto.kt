package com.example.myapplication.data.dto

import com.example.myapplication.data.model.orphanages.Need
import com.example.myapplication.data.model.orphanages.Priority
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val status: String = "active"
)

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

fun NeedDto.toNeed(): Need {
    return Need(
        id = id,
        category = categoryId,
        item = itemName,
        quantity = quantity,
        priority = Priority.valueOf(priority),
        description = description ?: ""
    )
}
