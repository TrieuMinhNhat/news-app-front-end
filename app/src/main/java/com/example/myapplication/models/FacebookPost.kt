package com.example.myapplication.models

import com.google.gson.annotations.SerializedName

data class FacebookPost(
    val id: Long,
    @SerializedName("source_name") val sourceName: String?,
    val content: String,
    val images: List<String> = emptyList(),
    @SerializedName("post_url") val postUrl: String?,
    @SerializedName("crawled_at") val crawledAt: String
)


