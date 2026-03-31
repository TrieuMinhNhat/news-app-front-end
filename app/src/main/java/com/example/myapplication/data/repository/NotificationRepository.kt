package com.example.myapplication.data.repository

import com.example.myapplication.service.apiService.NewsAPIService
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val api: NewsAPIService
) {
    suspend fun getNotifications(token: String) =
        api.getNotifications(token)

    suspend fun deleteNotification(id: Long, token: String) =
        api.deleteNotification(id, token)

    suspend fun markRead(id: Long, token: String) =
        api.markNotificationRead(id, token)

    suspend fun markReadByArticle(token: String, body: Map<String, String>) =
        api.markReadByArticle( token, body )

    suspend fun  markAllRead(token: String) =
        api.markAllRead(token)
}