package com.example.myapplication.ui.screens.home

import androidx.compose.runtime.Composable
import androidx.paging.compose.LazyPagingItems
import com.example.myapplication.models.FacebookPost
import com.example.myapplication.ui.screens.FacebookFeedList

@Composable
fun HomeSocialTab(
    posts: LazyPagingItems<FacebookPost>,
    refreshSignal: Int,
    availableKeywords: List<String>,
    selectedKeyword: String?,
    onKeywordSelected: (String?) -> Unit,
    savedPostIds: Set<Long> = emptySet(),
    onToggleSavedPost: ((FacebookPost) -> Unit)? = null
) {
    FacebookFeedList(
        posts = posts,
        refreshSignal = refreshSignal,
        availableKeywords = availableKeywords,
        selectedKeyword = selectedKeyword,
        onKeywordSelected = onKeywordSelected,
        savedPostIds = savedPostIds,
        onToggleSavedPost = onToggleSavedPost
    )
}
