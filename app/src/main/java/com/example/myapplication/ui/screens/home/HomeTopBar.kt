package com.example.myapplication.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.pager.PagerState
import com.example.myapplication.ui.components.TopicBar
import com.example.myapplication.ui.screens.NewsSearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    pagerState: PagerState,
    tabs: List<String>,
    isSearchExpanded: Boolean,
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onToggleSearch: () -> Unit,
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit,
    unreadCount: Int,
    savedTopics: List<String>,
    savedKeywords: List<String>,
    selectedTopic: String?,
    isInterestMode: Boolean,
    onTopicSelected: (String?) -> Unit,
    onInterestSelected: () -> Unit,
    onAddTopicClicked: () -> Unit,
    onTabSelected: (Int) -> Unit
) {
    Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
        TopAppBar(
            title = {
                Text(
                    "Hot News",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            navigationIcon = {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }
            },
            actions = {
                if (pagerState.currentPage == 0) {
                    IconButton(onClick = onToggleSearch) {
                        Icon(
                            imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (isSearchExpanded) "Close search" else "Search"
                        )
                    }
                }
                IconButton(onClick = onNotificationClick) {
                    BadgedBox(
                        badge = {
                            if (unreadCount > 0) {
                                Badge(
                                    modifier = Modifier
                                        .offset(x = (-4).dp, y = 4.dp)
                                        .height(20.dp)
                                        .defaultMinSize(minWidth = 20.dp)
                                ) {
                                    Text(
                                        text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        lineHeight = 11.sp
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Thông báo"
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        AnimatedVisibility(visible = pagerState.currentPage == 0 && isSearchExpanded) {
            NewsSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onSearch = onSearch
            )
        }

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
                    onClick = { onTabSelected(index) },
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
        AnimatedVisibility(visible = pagerState.currentPage == 0 && !isSearchExpanded && !isSearchActive) {
            TopicBar(
                savedTopics = savedTopics,
                savedKeywords = savedKeywords,
                selectedTopic = selectedTopic,
                isInterestMode = isInterestMode,
                onTopicSelected = onTopicSelected,
                onInterestSelected = onInterestSelected,
                onAddTopicClicked = onAddTopicClicked
            )
        }
    }
}
