package com.example.myapplication.data.repository

import com.example.myapplication.data.DeleteResponse
import com.example.myapplication.data.SavedStatusResponse
import com.example.myapplication.service.apiService.NewsAPIService
import javax.inject.Inject

class SavedRepository @Inject constructor(
    private val api: NewsAPIService
) {
    suspend fun listSavedArticles(token: String) =
        api.listSavedArticles(token)

    suspend fun saveArticle(token: String, articleId: Int) =
        api.saveArticle(token, articleId)

    suspend fun unsaveArticle(token: String, articleId: Int): DeleteResponse =
        api.unsaveArticle(token, articleId)

    suspend fun savedArticleStatus(token: String, articleId: Int): SavedStatusResponse =
        api.savedArticleStatus(token, articleId)

    suspend fun listSavedSocialPosts(token: String) =
        api.listSavedSocialPosts(token)

    suspend fun saveSocialPost(token: String, postId: Long) =
        api.saveSocialPost(token, postId)

    suspend fun unsaveSocialPost(token: String, postId: Long): DeleteResponse =
        api.unsaveSocialPost(token, postId)

    suspend fun savedSocialPostStatus(token: String, postId: Long): SavedStatusResponse =
        api.savedSocialPostStatus(token, postId)
}
