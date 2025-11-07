package com.example.myapplication.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Notification
import com.example.myapplication.data.model.NotificationType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<NotificationEvent?>(null)
    val events: StateFlow<NotificationEvent?> = _events.asStateFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(1000)
            
            // Mock notifications
            val notifications = listOf(
                Notification(
                    id = "1",
                    title = "Donation Confirmed",
                    message = "Your donation of \$50 to Hope Children's Home has been confirmed",
                    type = NotificationType.DONATION_CONFIRMED,
                    timestamp = System.currentTimeMillis() - 3600000,
                    isRead = false
                ),
                Notification(
                    id = "2",
                    title = "Thank You!",
                    message = "Hope Children's Home sent you a thank you message",
                    type = NotificationType.THANK_YOU_MESSAGE,
                    timestamp = System.currentTimeMillis() - 7200000,
                    isRead = false
                ),
                Notification(
                    id = "3",
                    title = "Need Updated",
                    message = "Little Angels Shelter updated their needs list",
                    type = NotificationType.NEED_UPDATED,
                    timestamp = System.currentTimeMillis() - 86400000,
                    isRead = true
                ),
                Notification(
                    id = "4",
                    title = "Donation Received",
                    message = "You received a donation of \$100",
                    type = NotificationType.DONATION_RECEIVED,
                    timestamp = System.currentTimeMillis() - 172800000,
                    isRead = true
                )
            )
            
            _uiState.update { it.copy(
                isLoading = false,
                notifications = notifications,
                unreadCount = notifications.count { !it.isRead }
            )}
        }
    }

    fun onNotificationClick(notification: Notification) {
        markAsRead(notification.id)
        _events.value = NotificationEvent.NavigateToDetail(notification)
    }

    fun onMarkAsRead(notificationId: String) {
        markAsRead(notificationId)
    }

    fun onMarkAllAsRead() {
        viewModelScope.launch {
            val updatedNotifications = _uiState.value.notifications.map {
                it.copy(isRead = true)
            }
            _uiState.update { it.copy(
                notifications = updatedNotifications,
                unreadCount = 0
            )}
        }
    }

    fun onDeleteNotification(notificationId: String) {
        viewModelScope.launch {
            val updatedNotifications = _uiState.value.notifications.filter { it.id != notificationId }
            _uiState.update { it.copy(
                notifications = updatedNotifications,
                unreadCount = updatedNotifications.count { !it.isRead }
            )}
        }
    }

    private fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            val updatedNotifications = _uiState.value.notifications.map {
                if (it.id == notificationId) it.copy(isRead = true) else it
            }
            _uiState.update { it.copy(
                notifications = updatedNotifications,
                unreadCount = updatedNotifications.count { !it.isRead }
            )}
        }
    }

    fun onEventHandled() {
        _events.value = null
    }
}

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0
)

sealed class NotificationEvent {
    data class NavigateToDetail(val notification: Notification) : NotificationEvent()
}
