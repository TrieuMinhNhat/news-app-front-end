package com.example.myapplication.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.SavedArticle
import com.example.myapplication.data.SavedSocialPost
import com.example.myapplication.data.UserPreferences
import com.example.myapplication.data.repository.SavedRepository
import com.example.myapplication.models.Article
import com.example.myapplication.models.FacebookPost
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class SavedViewModel @Inject constructor(
    private val repository: SavedRepository,
    private val userPrefs: UserPreferences
) : ViewModel() {

    private val _savedArticles = MutableStateFlow<List<Article>>(emptyList())
    val savedArticles: StateFlow<List<Article>> = _savedArticles.asStateFlow()

    private val _savedSocialPosts = MutableStateFlow<List<FacebookPost>>(emptyList())
    val savedSocialPosts: StateFlow<List<FacebookPost>> = _savedSocialPosts.asStateFlow()

    private val _savedArticleIds = MutableStateFlow<Set<Int>>(emptySet())
    val savedArticleIds: StateFlow<Set<Int>> = _savedArticleIds.asStateFlow()

    private val _savedSocialPostIds = MutableStateFlow<Set<Long>>(emptySet())
    val savedSocialPostIds: StateFlow<Set<Long>> = _savedSocialPostIds.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val deviceTokenFlow = userPrefs.fcmToken
        .map { it?.trim().orEmpty() }
        .filter { it.isNotEmpty() }
        .distinctUntilChanged()

    init {
        viewModelScope.launch {
            deviceTokenFlow.collectLatest { token ->
                refreshWithToken(token)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val token = getTokenOrNull() ?: return@launch
            refreshWithToken(token)
        }
    }

    fun toggleArticle(articleId: Int) {
        viewModelScope.launch {
            val token = getTokenOrNull() ?: return@launch
            val isSaved = _savedArticleIds.value.contains(articleId)
            try {
                if (isSaved) {
                    repository.unsaveArticle(token, articleId)
                } else {
                    repository.saveArticle(token, articleId)
                }
                refreshWithToken(token)
            } catch (e: Exception) {
                Log.e("SavedViewModel", "Failed to toggle article", e)
            }
        }
    }

    fun toggleSocialPost(postId: Long) {
        viewModelScope.launch {
            val token = getTokenOrNull() ?: return@launch
            val isSaved = _savedSocialPostIds.value.contains(postId)
            try {
                if (isSaved) {
                    repository.unsaveSocialPost(token, postId)
                } else {
                    repository.saveSocialPost(token, postId)
                }
                refreshWithToken(token)
            } catch (e: Exception) {
                Log.e("SavedViewModel", "Failed to toggle social post", e)
            }
        }
    }

    private fun updateSavedArticles(saved: List<SavedArticle>) {
        val articles = saved.map { it.article }
        _savedArticles.value = articles
        _savedArticleIds.value = articles.map { it.id }.toSet()
    }

    private fun updateSavedSocialPosts(saved: List<SavedSocialPost>) {
        val posts = saved.map { it.post }
        _savedSocialPosts.value = posts
        _savedSocialPostIds.value = posts.map { it.id }.toSet()
    }

    private suspend fun getTokenOrNull(): String? {
        return withTimeoutOrNull(5_000) { deviceTokenFlow.first() }
    }

    private suspend fun refreshWithToken(token: String) {
        _isLoading.value = true
        try {
            val savedArticles = repository.listSavedArticles(token)
            val savedSocialPosts = repository.listSavedSocialPosts(token)

            updateSavedArticles(savedArticles.results)
            updateSavedSocialPosts(savedSocialPosts.results)
        } catch (e: Exception) {
            Log.e("SavedViewModel", "Failed to refresh saved items", e)
        } finally {
            _isLoading.value = false
        }
    }
}
