package com.example.myapplication.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.example.myapplication.models.ArticlePagingSource
import com.example.myapplication.service.apiService.NewsAPIService
import javax.inject.Inject

class NewsRepository @Inject constructor(
    private val api: NewsAPIService
) {
    fun pager(keywords: String?, topic: String?) =
        Pager(PagingConfig(pageSize = 20)) {
            ArticlePagingSource(api, keywords = keywords, topic =  topic)
        }.flow

    suspend fun getArticleDetail(id: Int?) =
        api.getArticleDetail(id)
}