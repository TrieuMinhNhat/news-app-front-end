package com.example.myapplication.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.myapplication.models.Article
import com.example.myapplication.viewmodel.DeviceViewModel
import com.example.myapplication.viewmodel.NewsViewModel
import com.example.myapplication.viewmodel.NotificationViewModel
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import com.example.myapplication.viewmodel.FacebookViewModel
import kotlinx.coroutines.launch
import com.example.myapplication.ui.components.ArticleList
import com.example.myapplication.ui.components.TopicBar
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
/**
 * Màn hình chính hiển thị danh sách tin tức.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onArticleClicked: (Article) -> Unit,
    onMenuClicked: () -> Unit,
    onDebugClicked: () -> Unit,
    onNotificationIconClicked: () -> Unit,
    initialTabIndex: Int = 0,
    initialSocialKeyword: String? = null,
    newsViewModel: NewsViewModel = hiltViewModel(),
    deviceViewModel: DeviceViewModel = hiltViewModel(),
    facebookViewModel: FacebookViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel
) {
    val articles = newsViewModel.articlePager.collectAsLazyPagingItems()
    val facebookPosts = facebookViewModel.postPager.collectAsLazyPagingItems()
    val selectedFacebookKeyword by facebookViewModel.selectedKeyword.collectAsState()

    val savedTopics by deviceViewModel.savedTopics.collectAsState()
    val savedKeywords by deviceViewModel.savedKeywords.collectAsState()
    val selectedTopic by newsViewModel.selectedTopic.collectAsState()
    val isInterestMode by newsViewModel.isInterestMode.collectAsState()
    val selectedInterestKeyword by newsViewModel.selectedInterestKeyword.collectAsState()
    val notificationState by notificationViewModel.state.collectAsStateWithLifecycle()
    val searchQuery by newsViewModel.searchQuery.collectAsState()
    val tabs = listOf("Tin tức", "Mạng xã hội")
    val targetTab = initialTabIndex.coerceIn(0, tabs.lastIndex)
    val pagerState = rememberPagerState(initialPage = targetTab, pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    var newsRefreshSignal by remember { mutableIntStateOf(0) }
    var facebookRefreshSignal by remember { mutableIntStateOf(0) }

    // If navigation args change while this screen is already alive (app is active),
    // move the pager to the requested tab.
    LaunchedEffect(targetTab) {
        if (pagerState.currentPage != targetTab) {
            pagerState.animateScrollToPage(targetTab)
        }
    }

    LaunchedEffect(initialSocialKeyword) {
        if (!initialSocialKeyword.isNullOrBlank()) {
            facebookViewModel.onKeywordSelected(initialSocialKeyword)
            // Selecting a social keyword should also show the Social tab.
            if (pagerState.currentPage != 1) {
                pagerState.animateScrollToPage(1)
            }
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                TopAppBar(
                    title = {
                        Text("Hot News", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    },
                    navigationIcon = {
                        IconButton(onClick = onMenuClicked) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = onNotificationIconClicked) {
                            BadgedBox(
                                badge = {
                                    if (notificationState.unreadCount > 0) {
                                        Badge { Text(notificationState.unreadCount.toString()) }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = "Thông báo")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )

                PrimaryTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.surface,
                    indicator = {
                        TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(pagerState.currentPage),
                            width = 60.dp
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                val isRetap = index == pagerState.currentPage
                                if (isRetap) {
                                    if (index == 0) {
                                        newsRefreshSignal++
                                        articles.refresh()
                                    } else {
                                        facebookRefreshSignal++
                                        facebookPosts.refresh()
                                    }
                                } else {
                                    scope.launch { pagerState.animateScrollToPage(index) }
                                }
                            },
                            text = {
                                Text(
                                    text = title,
                                    style = if (pagerState.currentPage == index)
                                        MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    else MaterialTheme.typography.titleSmall
                                )
                            }
                        )
                    }
                }
                AnimatedVisibility(visible = pagerState.currentPage == 0){
                    TopicBar(
                        savedTopics = savedTopics.toList(),
                        savedKeywords = savedKeywords.toList(),
                        selectedTopic = selectedTopic,
                        isInterestMode = isInterestMode,
                        onTopicSelected = { newsViewModel.onTopicSelected(it) },
                        onInterestSelected = { newsViewModel.onInterestSelected(savedKeywords.toList()) },
                        onAddTopicClicked = onMenuClicked
                    )
                }

            }
        },
        bottomBar = {
            ProfessionalBottomBar(
                pagerState = pagerState,
                searchQuery = searchQuery,
                onSearchQueryChanged = newsViewModel::onSearchQueryChanged,
                onSearch = { newsViewModel.onSearchQueryChanged(searchQuery.trim()) }
            )

        }

    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalAlignment = Alignment.Top
        ) { pageIndex ->
            when (pageIndex) {
                0 -> { // NEWS PAGE
                    Column {

                        AnimatedVisibility(visible = isInterestMode && savedKeywords.isNotEmpty()) {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(savedKeywords.toList()) { keyword ->
                                    FilterChip(
                                        selected = selectedInterestKeyword == keyword,
                                        onClick = { newsViewModel.onInterestKeywordSelected(keyword) },
                                        label = { Text(keyword) }
                                    )
                                }
                            }
                        }
                        ArticleList(
                            articles = articles,
                            onArticleClicked = onArticleClicked,
                            contentPadding = PaddingValues(16.dp),
                            refreshSignal = newsRefreshSignal
                        )
                    }
                }
                1 -> { // FACEBOOK PAGE
                    FacebookFeedList(
                        posts = facebookPosts,
                        refreshSignal = facebookRefreshSignal,
                        availableKeywords = savedKeywords.toList(),
                        selectedKeyword = selectedFacebookKeyword,
                        onKeywordSelected = { facebookViewModel.onKeywordSelected(it) }
                    )
                }
            }
        }
    }
}







/**
 * Simple date formatter – adjust pattern as needed.
 */


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        // Mock data để preview
        // HomeScreen(...)
    }
}