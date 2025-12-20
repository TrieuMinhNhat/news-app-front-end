package com.example.myapplication.data

import com.google.gson.annotations.SerializedName

data class NotificationResponse(
    val id: Long,
    val title: String,
    val body: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("is_read") val isRead: Boolean,
    val article: Int?, // Django returns ID by default
    val data: Map<String, Any>?
)
