package com.example.myapplication.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.myapplication.data.UserPreferences
import com.example.myapplication.data.repository.NewsRepository
import com.example.myapplication.models.Article
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val repo: NewsRepository,
    private val userPrefs: UserPreferences
) : ViewModel() {
    val searchQuery = MutableStateFlow("")
    val selectedTopic = MutableStateFlow<String?>(null)
    val isInterestMode = MutableStateFlow(false)
    private val userKeywords = MutableStateFlow<List<String>>(emptyList())
    private val _selectedInterestKeyword = MutableStateFlow<String?>(null)
    val selectedInterestKeyword: StateFlow<String?> = _selectedInterestKeyword.asStateFlow()

    private val _headlines = MutableStateFlow<List<Article>>(emptyList())
    val headlines: StateFlow<List<Article>> = _headlines.asStateFlow()

    init {
        observeHeadlines()
    }
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
        val normalizedTopic = topic?.trim().takeUnless { it.isNullOrEmpty() || it == "All" }

        // Switching to topic/latest mode must always turn off interest mode.
        isInterestMode.value = false
        _selectedInterestKeyword.value = null

        selectedTopic.value = if (selectedTopic.value == normalizedTopic) {
            null
        } else {
            normalizedTopic
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

    private fun observeHeadlines() {
        viewModelScope.launch {
            val tokenFlow = userPrefs.fcmToken
                .map { it?.trim().orEmpty() }
                .distinctUntilChanged()

            val interestKeyFlow = combine(
                userPrefs.savedTopics,
                userPrefs.savedKeywords
            ) { topics, keywords ->
                (topics.toList().sorted() + keywords.toList().sorted()).joinToString("|")
            }.distinctUntilChanged()

            combine(tokenFlow, interestKeyFlow) { token, _ -> token }
                .distinctUntilChanged()
                .collectLatest { token ->
                    if (token.isBlank()) {
                        FirebaseMessaging.getInstance().token.addOnSuccessListener { newToken ->
                            viewModelScope.launch {
                                userPrefs.saveToken(newToken)
                            }
                        }
                        return@collectLatest
                    }
                    try {
                        val response = repo.getHeadlines(token)
                        _headlines.value = response.results
                    } catch (e: Exception) {
                        _headlines.value = emptyList()
                    }
                }
        }
    }

    fun refreshHeadlines() {
        viewModelScope.launch {
            val token = userPrefs.fcmToken.first()?.trim().orEmpty()
            if (token.isBlank()) {
                FirebaseMessaging.getInstance().token.addOnSuccessListener { newToken ->
                    viewModelScope.launch {
                        userPrefs.saveToken(newToken)
                    }
                }
                return@launch
            }
            try {
                val response = repo.getHeadlines(token)
                _headlines.value = response.results
            } catch (e: Exception) {
                _headlines.value = emptyList()
            }
        }
    }

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