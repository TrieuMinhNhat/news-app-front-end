package com.example.myapplication.data

import com.google.gson.annotations.SerializedName

data class UnreadCountResponse(
    @SerializedName("unread_count") val unreadCount: Int
)