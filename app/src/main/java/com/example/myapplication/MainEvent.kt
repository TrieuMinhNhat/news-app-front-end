package com.example.myapplication

sealed interface MainEvent {
    data class NotificationArrived(
        val articleId: String?,
        val notificationType: String?,
        val notificationId: String?,
        val keyword: String?
    ) : MainEvent
    object RefreshNotification : MainEvent
}

