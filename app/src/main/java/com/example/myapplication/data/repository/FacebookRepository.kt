package com.example.myapplication.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.example.myapplication.models.FacebookPagingSource
import com.example.myapplication.service.apiService.NewsAPIService
import javax.inject.Inject

class FacebookRepository @Inject constructor(
    private val api: NewsAPIService
) {

    fun getFacebookPosts(
        token: String,
        keywords: String? = null,
        sortKeyword: String? = null
    ) =
        Pager(PagingConfig(pageSize = 20)) {
            FacebookPagingSource(
                api,
                token = token,
                keywords = keywords,
                sortKeyword = sortKeyword
            )
        }.flow
}
