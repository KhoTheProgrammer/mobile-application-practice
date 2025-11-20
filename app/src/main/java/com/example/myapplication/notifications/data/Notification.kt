package com.example.myapplication.notifications.data

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: Long,
    val isRead: Boolean = false,
    val relatedId: String? = null // donation ID, orphanage ID, etc.
)

enum class NotificationType {
    DONATION_RECEIVED,
    DONATION_CONFIRMED,
    NEED_UPDATED,
    THANK_YOU_MESSAGE,
    SYSTEM_UPDATE,
    REMINDER
}
