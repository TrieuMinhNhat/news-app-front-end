package com.example.myapplication.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.LoadState
import com.example.myapplication.models.Article
import com.example.myapplication.ui.components.ArticleList
import com.example.myapplication.ui.components.TopHeadlinesCarousel

@Composable
fun HomeNewsTab(
    isInterestMode: Boolean,
    savedKeywords: List<String>,
    selectedInterestKeyword: String?,
    onInterestKeywordSelected: (String) -> Unit,
    articles: LazyPagingItems<Article>,
    onArticleClicked: (Article) -> Unit,
    isSearchExpanded: Boolean,
    isSearchActive: Boolean,
    showHeadlines: Boolean,
    headlineArticles: List<Article>,
    refreshSignal: Int,
    onRefresh: () -> Unit
) {
    Column {
        AnimatedVisibility(visible = !isSearchExpanded && !isSearchActive && isInterestMode && savedKeywords.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(savedKeywords) { keyword ->
                    FilterChip(
                        selected = selectedInterestKeyword == keyword,
                        onClick = { onInterestKeywordSelected(keyword) },
                        label = { Text(keyword) }
                    )
                }
            }
        }
        if (isSearchActive && articles.itemCount == 0 && articles.loadState.refresh is LoadState.NotLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Không tìm thấy kết quả nào.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            ArticleList(
                articles = articles,
                onArticleClicked = onArticleClicked,
                contentPadding = PaddingValues(16.dp),
                refreshSignal = refreshSignal,
                onRefresh = onRefresh,
                headerContent = if (!isSearchExpanded && !isSearchActive && showHeadlines && headlineArticles.isNotEmpty()) {
                    {
                        TopHeadlinesCarousel(
                            headlines = headlineArticles,
                            onArticleClick = onArticleClicked
                        )
                    }
                } else {
                    null
                },
                skipCount = 0,
                excludedArticleIds = if (showHeadlines) {
                    headlineArticles.map { it.id }.toSet()
                } else {
                    emptySet()
                }
            )
        }
    }
}
