package com.example.myapplication.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.example.myapplication.models.Article
import com.example.myapplication.models.ArticlePagingSource
import com.example.myapplication.service.apiService.NewsAPIService

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NewsViewModel  : ViewModel(){
    private val apiService by lazy {
        Retrofit.Builder()
            .baseUrl(NewsAPIService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsAPIService::class.java)
    }
    // State for the current search query (null means show all)
    val searchQuery = MutableStateFlow("")
    val selectedTopic = MutableStateFlow<String?>(null)
    val isInterestMode = MutableStateFlow(false)
    private val userKeywords = MutableStateFlow<List<String>>(emptyList())
    // Use flatMapLatest to restart the Pager whenever searchQuery changes
    @OptIn(ExperimentalCoroutinesApi::class)
    val articlePager = combine(
        searchQuery, selectedTopic, isInterestMode, userKeywords
    ) { query, topic, interestMode, keywords ->
        Triple(query, topic, if (interestMode) keywords else emptyList())
    }.flatMapLatest { (query, topic, keywords) ->

        val finalKeywords: String? = if (keywords.isNotEmpty()) keywords.joinToString(",") else null
        val finalTopic: String? = if (finalKeywords == null && topic != null) topic else null

        Pager(PagingConfig(pageSize = 20)) {
            ArticlePagingSource(
                apiService,
                keywords = finalKeywords,
                topic = finalTopic
            )
        }.flow
    }.cachedIn(viewModelScope)

    fun onTopicSelected(topic: String?) {
        if (selectedTopic.value == topic) {
            selectedTopic.value = null
        } else {
            selectedTopic.value = topic
            isInterestMode.value = false
        }
        searchQuery.value = ""
    }
    fun setTopic(topic: String?) {
        selectedTopic.value = if (topic.isNullOrBlank() || topic == "All") null else topic
    }
    fun onInterestSelected(keywords: List<String>) {
        if (isInterestMode.value) {
            isInterestMode.value = false
        } else {
            userKeywords.value = keywords
            isInterestMode.value = true
            selectedTopic.value = null
        }
        searchQuery.value = ""
    }

    fun onSearchQueryChanged(newQuery: String) {
        searchQuery.value = newQuery
    }

    private val _articleDetail = MutableStateFlow<Article?>(null)
    val articleDetail: StateFlow<Article?> = _articleDetail.asStateFlow()

    private val _isLoadingDetail = MutableStateFlow(false)
    val isLoadingDetail: StateFlow<Boolean> = _isLoadingDetail.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /** Gọi API để lấy chi tiết bài báo */
    fun fetchArticleDetail(articleId: Int?) {
        viewModelScope.launch {
            _isLoadingDetail.value = true
            _errorMessage.value = null
            try {
                val article = apiService.getArticleDetail(articleId)
                _articleDetail.value = article
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = e.message ?: "Unknown error"
            } finally {
                _isLoadingDetail.value = false
            }
        }
    }
}