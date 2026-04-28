package com.example.myapplication.data

data class NotificationUiModel(
    val id: Long,
    val title: String,
    val body: String,
    val timestamp: String, // Ví dụ: "10:30" hoặc "2 giờ trước"
    val isRead: Boolean,
    val articleId: String?, // Để navigate khi click vào
    val type: String?,
    val keyword: String?
)
