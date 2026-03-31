package com.example.myapplication.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.myapplication.data.repository.NewsRepository
import com.example.myapplication.models.Article
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val repo: NewsRepository
) : ViewModel() {
    val searchQuery = MutableStateFlow("")
    val selectedTopic = MutableStateFlow<String?>(null)
    val isInterestMode = MutableStateFlow(false)
    private val userKeywords = MutableStateFlow<List<String>>(emptyList())
    private val _selectedInterestKeyword = MutableStateFlow<String?>(null)
    val selectedInterestKeyword: StateFlow<String?> = _selectedInterestKeyword.asStateFlow()
    @OptIn(ExperimentalCoroutinesApi::class)
    val articlePager = combine(
        searchQuery, selectedTopic, isInterestMode, userKeywords, _selectedInterestKeyword
    ) { query, topic, interestMode, keywords, selectedInterestKeyword ->
        QueryState(
            query = query,
            topic = topic,
            interestMode = interestMode,
            keywords = keywords,
            selectedInterestKeyword = selectedInterestKeyword
        )
    }.flatMapLatest { state ->

        // 🔥 FIX: Use 'query' if it exists.
        // Order of priority: Search Query > Interest Keywords > None
        val finalKeywords: String? = when {
            state.query.isNotBlank() -> state.query
            state.interestMode && !state.selectedInterestKeyword.isNullOrBlank() -> state.selectedInterestKeyword
            state.interestMode && state.keywords.isNotEmpty() -> state.keywords.joinToString(",")
            else -> null
        }

        // Keep existing logic: If we have keywords (Search or Interest), ignore specific topic
        // unless your API supports filtering Topic + Keywords together.
        val finalTopic: String? = if (finalKeywords == null && state.topic != null) state.topic else null
        repo.pager(finalKeywords,finalTopic)
    }.cachedIn(viewModelScope)

    fun onTopicSelected(topic: String?) {
        if (selectedTopic.value == topic) {
            selectedTopic.value = null
        } else {
            selectedTopic.value = topic
            isInterestMode.value = false
            _selectedInterestKeyword.value = null
        }
        searchQuery.value = ""
    }
    fun setTopic(topic: String?) {
        selectedTopic.value = if (topic.isNullOrBlank() || topic == "All") null else topic
    }
    fun onInterestSelected(keywords: List<String>) {
        if (isInterestMode.value) {
            isInterestMode.value = false
            _selectedInterestKeyword.value = null
        } else {
            userKeywords.value = keywords
            isInterestMode.value = true
            _selectedInterestKeyword.value = null
            selectedTopic.value = null
        }
        searchQuery.value = ""
    }

    fun onInterestKeywordSelected(keyword: String?) {
        val normalizedKeyword = keyword?.trim().takeUnless { it.isNullOrEmpty() }
        _selectedInterestKeyword.value =
            if (_selectedInterestKeyword.value == normalizedKeyword) null else normalizedKeyword
    }

    fun onSearchQueryChanged(newQuery: String) {
        searchQuery.value = newQuery
    }

    private data class QueryState(
        val query: String,
        val topic: String?,
        val interestMode: Boolean,
        val keywords: List<String>,
        val selectedInterestKeyword: String?
    )

    private val _articleDetail = MutableStateFlow<Article?>(null)
    val articleDetail: StateFlow<Article?> = _articleDetail.asStateFlow()

    private val _isLoadingDetail = MutableStateFlow(false)
    val isLoadingDetail: StateFlow<Boolean> = _isLoadingDetail.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

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