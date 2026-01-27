package com.example.myapplication.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.myapplication.data.repository.NewsRepository
import com.example.myapplication.models.Article
import com.example.myapplication.service.apiService.RetrofitProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class NewsViewModel  : ViewModel(){

    private val repo = NewsRepository(
        RetrofitProvider.apiService
    )
    val searchQuery = MutableStateFlow("")
    val selectedTopic = MutableStateFlow<String?>(null)
    val isInterestMode = MutableStateFlow(false)
    private val userKeywords = MutableStateFlow<List<String>>(emptyList())
    @OptIn(ExperimentalCoroutinesApi::class)
    val articlePager = combine(
        searchQuery, selectedTopic, isInterestMode, userKeywords
    ) { query, topic, interestMode, keywords ->
        Triple(query, topic, if (interestMode) keywords else emptyList())
    }.flatMapLatest { (query, topic, keywords) ->

        val finalKeywords: String? = if (keywords.isNotEmpty()) keywords.joinToString(",") else null
        val finalTopic: String? = if (finalKeywords == null && topic != null) topic else null

        repo.pager(finalKeywords,finalTopic)
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
                val article = repo.getArticleDetail(articleId)
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