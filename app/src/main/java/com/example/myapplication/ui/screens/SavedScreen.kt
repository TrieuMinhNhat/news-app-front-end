package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.models.Article
import com.example.myapplication.models.FacebookPost
import com.example.myapplication.ui.components.ArticleCard
import com.example.myapplication.viewmodel.SavedViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    onBack: () -> Unit,
    onArticleClicked: (Article) -> Unit,
    viewModel: SavedViewModel = hiltViewModel()
) {
    val savedArticles by viewModel.savedArticles.collectAsState()
    val savedSocialPosts by viewModel.savedSocialPosts.collectAsState()
    val savedSocialPostIds by viewModel.savedSocialPostIds.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val tabs = listOf("Bài báo", "Mạng xã hội")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đã lưu") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(title) }
                    )
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> SavedArticlesTab(
                            articles = savedArticles,
                            onArticleClicked = onArticleClicked,
                            onUnsave = { article ->
                                viewModel.toggleArticle(article.id)
                            }
                        )

                        1 -> SavedSocialPostsTab(
                            posts = savedSocialPosts,
                            savedPostIds = savedSocialPostIds,
                            onToggleSaved = { post ->
                                viewModel.toggleSocialPost(post.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SavedArticlesTab(
    articles: List<Article>,
    onArticleClicked: (Article) -> Unit,
    onUnsave: (Article) -> Unit
) {
    if (articles.isEmpty()) {
        EmptySavedState("Bạn chưa lưu bài báo nào.")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(articles, key = { it.id }) { article ->
                Box {
                    ArticleCard(
                        article = article,
                        onClick = { onArticleClicked(article) }
                    )

                    IconButton(
                        onClick = { onUnsave(article) },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Bỏ lưu",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SavedSocialPostsTab(
    posts: List<FacebookPost>,
    savedPostIds: Set<Long>,
    onToggleSaved: (FacebookPost) -> Unit
) {
    if (posts.isEmpty()) {
        EmptySavedState("Bạn chưa lưu bài đăng mạng xã hội nào.")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(posts, key = { it.id }) { post ->
                FacebookPostCard(
                    post = post,
                    isSaved = savedPostIds.contains(post.id),
                    onToggleSaved = { onToggleSaved(post) }
                )
            }
        }
    }
}

@Composable
fun EmptySavedState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
