package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
        val totalCount = articles.itemCount
        val visibleCount = (totalCount - skipCount).coerceAtLeast(0)
        val isEmpty = articles.loadState.refresh is LoadState.NotLoading &&
            visibleCount == 0

        if (isEmpty) {
            ArticleEmptyState(modifier = Modifier.fillMaxSize())
        } else {
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
                        count = visibleCount,
                        key = { index ->
                            val actualIndex = index + skipCount
                            val item = if (actualIndex < totalCount) {
                                articles.itemSnapshotList.items.getOrNull(actualIndex)
                            } else {
                                null
                            }
                            item?.id ?: actualIndex
                        }
                    ) { index ->
                        val actualIndex = index + skipCount
                        if (actualIndex >= totalCount) {
                            return@items
                        }

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
}

@Composable
private fun ArticleEmptyState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outlineVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Chưa có bài viết phù hợp",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Hiện chưa có bài viết nào phù hợp với chủ đề hoặc từ khóa bạn đã chọn.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}