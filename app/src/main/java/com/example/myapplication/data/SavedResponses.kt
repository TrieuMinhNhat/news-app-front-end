package com.example.myapplication.data

import com.example.myapplication.models.Article
import com.example.myapplication.models.FacebookPost
import com.google.gson.annotations.SerializedName

data class SavedArticleResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<SavedArticle>
)

data class SavedSocialPostResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<SavedSocialPost>
)

data class SavedArticle(
    val id: Int,
    val article: Article,
    @SerializedName("created_at") val createdAt: String
)

data class SavedSocialPost(
    val id: Int,
    val post: FacebookPost,
    @SerializedName("created_at") val createdAt: String
)

data class SavedStatusResponse(
    @SerializedName("is_saved") val isSaved: Boolean
)

data class DeleteResponse(
    val deleted: Boolean
)
