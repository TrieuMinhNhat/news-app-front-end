package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.myapplication.data.UserPreferences
import com.example.myapplication.models.FacebookPost
import com.example.myapplication.data.repository.FacebookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject


@HiltViewModel
class FacebookViewModel @Inject constructor(
    private val repository: FacebookRepository,
    private val userPrefs: UserPreferences
) : ViewModel() {

    private val _selectedKeyword = MutableStateFlow<String?>(null)
    val selectedKeyword: StateFlow<String?> = _selectedKeyword.asStateFlow()

    private val deviceTokenFlow = userPrefs.fcmToken
        .map { it?.trim().orEmpty() }
        .filter { it.isNotEmpty() }
        .distinctUntilChanged()

    private val savedKeywordsFlow = userPrefs.savedKeywords
        .map { keywords ->
            keywords
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .distinct()
                .sorted()
        }
        .distinctUntilChanged()

    val postPager = combine(deviceTokenFlow, _selectedKeyword, savedKeywordsFlow) { token, selectedKeyword, savedKeywords ->
        Triple(token, selectedKeyword, savedKeywords)
    }
        .flatMapLatest { (token, selectedKeyword, savedKeywords) ->
            val requestKeywords = selectedKeyword
                ?: savedKeywords.joinToString(",").takeIf { it.isNotBlank() }

            if (requestKeywords == null) {
                flowOf(PagingData.empty())
            } else {
                repository.getFacebookPosts(
                    token = token,
                    keywords = requestKeywords,
                    sortKeyword = selectedKeyword
                )
            }
        }
        .cachedIn(viewModelScope)

    fun onKeywordSelected(keyword: String?) {
        val normalizedKeyword = keyword?.trim().takeUnless { it.isNullOrEmpty() }
        if (_selectedKeyword.value == normalizedKeyword) return
        _selectedKeyword.value = normalizedKeyword
    }
}