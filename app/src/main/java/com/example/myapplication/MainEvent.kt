package com.example.myapplication

sealed interface MainEvent {
    data class NotificationArrived(val articleId: String) : MainEvent
    object RefreshNotification : MainEvent
}

