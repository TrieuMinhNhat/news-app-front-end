package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
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
    modifier: Modifier = Modifier
) {

    val isRefreshing = articles.loadState.refresh is LoadState.Loading

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { articles.refresh() }
    ) {

        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = contentPadding
        ) {

            items(
                count = articles.itemCount,
                key = { index -> articles[index]?.id ?: index }
            ) { index ->

                val article = articles[index]

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