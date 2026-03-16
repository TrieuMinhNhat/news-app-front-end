package com.example.myapplication.models

import com.google.gson.annotations.SerializedName

data class Article(
    val id: Int,   // <- match với Django AutoField
    val title: String,
    val link: String,
    val summary: String? = null,
    val published: String? = null,
    @SerializedName("full_content") val description: String = "",
    @SerializedName("image_link") val imageUrl: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val author: String? = null,
    @SerializedName("publisher") val source: String? = null,
)
