package com.example.myapplication.models

// ArticlePagingSource.kt
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlin.let
import com.example.myapplication.service.apiService.NewsAPIService
class ArticlePagingSource(
    private val apiService: NewsAPIService,
    private val query: String? = null,
    private val topic: String? = null, // Đã đổi tên thành topic
    private val keywords: String? = null

) : PagingSource<Int, Article>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        return try {
            val page = params.key ?: 1
            val response = if (!query.isNullOrBlank()) {
                // 🔍 SEARCH MODE
                apiService.searchArticles(
                    query = query,
                    page = page
                )
            } else {
                // 📰 NORMAL MODE
                apiService.getArticles(
                    page = page,
                    topic = topic,
                    keywords = keywords
                )
            }
            LoadResult.Page(
                data = response.results,
                prevKey = if (page > 1) page - 1 else null,
                nextKey = if (response.next != null) page + 1 else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
//tim mạch, đột quỵ,