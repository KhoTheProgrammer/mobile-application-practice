package com.example.myapplication.orphanage.data

/**
 * A data class that represents a need.
 *
 * @param item The name of the item.
 * @param quantity The quantity of the item.
 * @param priority The priority of the need.
 * @param description A description of the need.
 */
data class Need(
    val item: String,
    val quantity: Int,
    val priority: Priority,
    val description: String
)

/**
 * An enum that represents the priority of a need.
 */
enum class Priority {
    URGENT,
    HIGH,
    MEDIUM,
    LOW
}
