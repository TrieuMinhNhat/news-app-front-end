package com.example.myapplication.models

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.myapplication.service.apiService.NewsAPIService

class FacebookPagingSource(
    private val apiService: NewsAPIService
) : PagingSource<Int, FacebookPost>() {

    override fun getRefreshKey(state: PagingState<Int, FacebookPost>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FacebookPost> {
        return try {
            val page = params.key ?: 1
            val response = apiService.getFacebookPosts(page = page)

            // Check if there is a next page based on the "next" field being null or not
            val nextKey = if (response.next != null) page + 1 else null

            LoadResult.Page(
                data = response.results,
                prevKey = if (page == 1) null else page - 1,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}