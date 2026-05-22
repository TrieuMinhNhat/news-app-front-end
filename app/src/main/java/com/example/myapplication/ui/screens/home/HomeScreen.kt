package com.example.myapplication.ui.screens.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.myapplication.models.Article
import com.example.myapplication.viewmodel.DeviceViewModel
import com.example.myapplication.viewmodel.FacebookViewModel
import com.example.myapplication.viewmodel.NewsViewModel
import com.example.myapplication.viewmodel.NotificationViewModel
import kotlinx.coroutines.launch

/**
 * Màn hình chính hiển thị danh sách tin tức.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onArticleClicked: (Article) -> Unit,
    onInterestClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
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
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var newsRefreshSignal by remember { mutableIntStateOf(0) }
    var facebookRefreshSignal by remember { mutableIntStateOf(0) }
    var isSearchExpanded by rememberSaveable { mutableStateOf(false) }
    val headlineArticles by newsViewModel.headlines.collectAsState()
    val showHeadlines = !isInterestMode && selectedTopic.isNullOrBlank()

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

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != 0 && isSearchExpanded) {
            isSearchExpanded = false
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HomeDrawerContent(
                onInterestClicked = {
                    scope.launch { drawerState.close() }
                    onInterestClicked()
                },
                onSettingsClicked = {
                    scope.launch { drawerState.close() }
                    onSettingsClicked()
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                HomeTopBar(
                    pagerState = pagerState,
                    tabs = tabs,
                    isSearchExpanded = isSearchExpanded,
                    searchQuery = searchQuery,
                    onSearchQueryChange = newsViewModel::onSearchQueryChanged,
                    onSearch = {
                        newsViewModel.onSearchQueryChanged(searchQuery.trim())
                        isSearchExpanded = false
                    },
                    onToggleSearch = { isSearchExpanded = !isSearchExpanded },
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onNotificationClick = onNotificationIconClicked,
                    unreadCount = notificationState.unreadCount,
                    savedTopics = savedTopics.toList(),
                    savedKeywords = savedKeywords.toList(),
                    selectedTopic = selectedTopic,
                    isInterestMode = isInterestMode,
                    onTopicSelected = newsViewModel::onTopicSelected,
                    onInterestSelected = { newsViewModel.onInterestSelected(savedKeywords.toList()) },
                    onAddTopicClicked = onInterestClicked,
                    onTabSelected = { index ->
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
                    }
                )
            }
        ) { innerPadding ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) { pageIndex ->
                when (pageIndex) {
                    0 -> {
                        HomeNewsTab(
                            isInterestMode = isInterestMode,
                            savedKeywords = savedKeywords.toList(),
                            selectedInterestKeyword = selectedInterestKeyword,
                            onInterestKeywordSelected = newsViewModel::onInterestKeywordSelected,
                            articles = articles,
                            onArticleClicked = onArticleClicked,
                            showHeadlines = showHeadlines,
                            headlineArticles = headlineArticles,
                            refreshSignal = newsRefreshSignal,
                            onRefresh = newsViewModel::refreshHeadlines
                        )
                    }
                    1 -> {
                        HomeSocialTab(
                            posts = facebookPosts,
                            refreshSignal = facebookRefreshSignal,
                            availableKeywords = savedKeywords.toList(),
                            selectedKeyword = selectedFacebookKeyword,
                            onKeywordSelected = facebookViewModel::onKeywordSelected
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    // Preview intentionally empty.
}
