package com.example.myapplication.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.example.myapplication.models.FacebookPagingSource
import com.example.myapplication.service.apiService.RetrofitProvider

class FacebookRepository {

    private val api = RetrofitProvider.apiService

    fun getFacebookPosts() =
        Pager(PagingConfig(pageSize = 20)) {
            FacebookPagingSource(api)
        }.flow
}
