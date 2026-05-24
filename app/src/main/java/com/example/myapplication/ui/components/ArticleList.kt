package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
    skipCount: Int = 0,
    excludedArticleIds: Set<Int> = emptySet()
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
            verticalArrangement = Arrangement.spacedBy(0.dp),
            contentPadding = contentPadding
        ) {

            if (headerContent != null) {
                item {
                    headerContent()
                }
            }
            if (articles.loadState.refresh is LoadState.Error) {
                val errorState = articles.loadState.refresh as LoadState.Error
                item {
                    Column(
                        modifier = Modifier.fillParentMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Lỗi kết nối: ${errorState.error.localizedMessage}")
                        Button(onClick = { articles.retry() }) {
                            Text("Thử lại")
                        }
                    }
                }
            } else {
                items(
                    count = (articles.itemCount - skipCount).coerceAtLeast(0),
                    key = { index ->
                        val actualIndex = index + skipCount
                        articles[actualIndex]?.id ?: actualIndex
                    }
                ) { index ->
                    val actualIndex = index + skipCount
                    val article = articles[actualIndex]

                    if (article != null && !excludedArticleIds.contains(article.id)) {
                        Column {
                            ArticleCard(
                                article = article,
                                onClick = { onArticleClicked(article) }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 20.dp),
                                thickness = 2.dp,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        }
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
}