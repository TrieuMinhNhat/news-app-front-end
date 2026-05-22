package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.example.myapplication.models.Article

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleList(
    articles: LazyPagingItems<Article>,
    onArticleClicked: (Article) -> Unit,
    contentPadding: PaddingValues,
    refreshSignal: Int = 0,
    onRefresh: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    headerContent: (@Composable () -> Unit)? = null,
    skipCount: Int = 0
) {
    var isUserRefreshing by remember { mutableStateOf(false) }
    val isRefreshLoading = articles.loadState.refresh is LoadState.Loading
    val listState = rememberLazyListState()

    LaunchedEffect(isRefreshLoading) {
        if (!isRefreshLoading) {
            isUserRefreshing = false
        }
    }

    LaunchedEffect(refreshSignal) {
        if (refreshSignal > 0) {
            listState.animateScrollToItem(0)
        }
    }

    PullToRefreshBox(
        isRefreshing = isUserRefreshing && isRefreshLoading,
        onRefresh = {
            isUserRefreshing = true
            onRefresh?.invoke()
            articles.refresh()
        }
    ) {

        LazyColumn(
            state = listState,
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = contentPadding
        ) {

            if (headerContent != null) {
                item {
                    headerContent()
                }
            }

            val visibleCount = (articles.itemCount - skipCount).coerceAtLeast(0)

            items(
                count = visibleCount,
                key = { index ->
                    val actualIndex = index + skipCount
                    articles[actualIndex]?.id ?: actualIndex
                }
            ) { index ->

                val article = articles[index + skipCount]

                article?.let {
                    ArticleCard(
                        article = it,
                        onClick = { onArticleClicked(it) }
                    )
                }
            }

            if (articles.loadState.append is LoadState.Loading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}