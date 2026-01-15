package com.example.myapplication.ui.screens

import DeviceViewModel
import android.R.attr.contentDescription
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.FiberNew
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.example.myapplication.models.Article
import com.example.myapplication.viewmodel.NewsViewModel
import com.example.myapplication.viewmodel.NotificationViewModel

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

    newsViewModel: NewsViewModel = viewModel(),
    deviceViewModel: DeviceViewModel = viewModel(),

    notificationViewModel: NotificationViewModel
) {
    val articles = newsViewModel.articlePager.collectAsLazyPagingItems()
    val savedTopics by deviceViewModel.savedTopics.collectAsState()
    val savedKeywords by deviceViewModel.savedKeywords.collectAsState()
    val selectedTopic by newsViewModel.selectedTopic.collectAsState()
    val isInterestMode by newsViewModel.isInterestMode.collectAsState()
    val notificationState by notificationViewModel.state.collectAsStateWithLifecycle()
    val unreadCount = notificationState.unreadCount
    val searchQuery by newsViewModel.searchQuery.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Tin tức hôm nay",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClicked) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Navigation Menu"
                        )
                    }
                },
                // --- PHẦN THÊM VÀO ---
                actions = {
                    IconButton(onClick = onNotificationIconClicked) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge {
                                        Text(text = unreadCount.toString())
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
                colors = TopAppBarDefaults.topAppBarColors(
                    // Màu nền trùng với Surface của TopicBar để tạo cảm giác liền mạch
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),

            )
        },
//        floatingActionButton = {
//            FloatingActionButton(
//                onClick = onDebugClicked,
//                containerColor = MaterialTheme.colorScheme.primary,
//                contentColor = MaterialTheme.colorScheme.onPrimary
//            ) {
//                Icon(Icons.Default.Build, contentDescription = "Debug Info")
//            }
//        }
    ) { innerPadding ->
        // Cấu trúc layout chính
        Column(
            modifier = Modifier
                .fillMaxSize()
                // CHỈ LẤY padding TOP để tránh thanh Topic bị đè,
                // KHÔNG lấy padding ngang/đáy ở đây để TopicBar tràn viền.
                .padding(top = innerPadding.calculateTopPadding())
        ) {

            // 1. Thanh chủ đề (Topic Bar) - Tràn viền ngang
            PersonalizedTopicBar(
                savedTopics = savedTopics.toList(),
                savedKeywords = savedKeywords,
                selectedTopic = selectedTopic,
                isInterestMode = isInterestMode,
                onTopicSelected = { newsViewModel.onTopicSelected(it) },
                onInterestSelected = { newsViewModel.onInterestSelected(savedKeywords) },
                onAddTopicClicked = onMenuClicked
            )

            NewsSearchBar(
                query = searchQuery,
                onQueryChange = { newsViewModel.onSearchQueryChanged(it) } //
            )

            // 2. Danh sách bài báo
            ArticleList(
                articles = articles,
                onArticleClicked = onArticleClicked,
                // Đẩy padding đáy xuống đây để list không bị che bởi NavigationBar/FAB
                contentPadding = PaddingValues(
                    bottom = innerPadding.calculateBottomPadding() + 80.dp, // +80dp để né FAB
                    top = 16.dp,
                    start = 16.dp,
                    end = 16.dp
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalizedTopicBar(
    savedTopics: List<String>,
    savedKeywords: List<String>,
    selectedTopic: String?,
    isInterestMode: Boolean,
    onTopicSelected: (String) -> Unit,
    onInterestSelected: () -> Unit,
    onAddTopicClicked: () -> Unit
) {
    // Surface tạo nền và đổ bóng nhẹ tách biệt với nội dung bên dưới
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // -- Nút "Dành cho bạn" (AI Interest) --
            if (savedKeywords.isNotEmpty()) {
                item {
                    FilterChip(
                        selected = isInterestMode,
                        onClick = onInterestSelected,
                        label = { Text("Dành cho bạn") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AcUnit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            // Làm nổi bật nhẹ kể cả khi chưa chọn
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isInterestMode,
                            borderColor = if (isInterestMode) Color.Transparent else MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }
            }

            // -- Nút "Mới nhất" (Default) --
            item {
                FilterChip(
                    selected = selectedTopic == null && !isInterestMode,
                    onClick = { onTopicSelected("") },
                    label = { Text("Mới nhất") },
                    leadingIcon = {
                        // Chỉ hiện icon khi đang chọn tab này
                        if (selectedTopic == null && !isInterestMode) {
                            Icon(Icons.Default.FiberNew, contentDescription = null, Modifier.size(18.dp))
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }

            // -- Các Topic người dùng đã lưu --
            items(savedTopics) { topic ->
                FilterChip(
                    selected = selectedTopic == topic,
                    onClick = { onTopicSelected(topic) },
                    label = { Text(topic) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }

            // -- Nút Thêm chủ đề (Cuối danh sách) --
            item {
                SuggestionChip(
                    onClick = onAddTopicClicked,
                    label = { Text("Thêm chủ đề") },
                    icon = { Icon(Icons.Default.Add, contentDescription = null, Modifier.size(16.dp)) }
                )
            }
        }
    }
}

@Composable
fun ArticleList(
    articles: LazyPagingItems<Article>,
    onArticleClicked: (Article) -> Unit,
    contentPadding: PaddingValues, // Nhận padding từ bên ngoài
    modifier: Modifier = Modifier
) {
    if (articles.itemCount == 0) {
        // Xử lý khi loading hoặc danh sách rỗng
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = contentPadding
        ) {
            items(articles.itemCount) { index ->
                val article = articles[index]
                if (article != null) {
                    ArticleCard(
                        article = article,
                        onClick = { onArticleClicked(article) }
                    )
                }
            }
        }
    }
}

@Composable
fun ArticleCard(
    article: Article,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium, // Bo góc 12-16dp tuỳ theme
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Phần Ảnh
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp) // Tăng chiều cao ảnh một chút
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                val imageModel = if (article.imageUrl.isNotEmpty()) article.imageUrl[0] else null
                AsyncImage(
                    model = imageModel,
                    contentDescription = "Ảnh bài báo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    // Xử lý khi load lỗi hoặc đang load (Tuỳ chọn)
                    // error = painterResource(R.drawable.error_placeholder)
                )
            }

            // Phần Nội dung chữ
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Nguồn tin / Tác giả (Chip nhỏ hoặc Text nhỏ)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = (article.author ?: "News").uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
//                    Text(
//                        text = "• ${article.author ?: "Unknown"}",
//                        style = MaterialTheme.typography.labelSmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant,
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis
//                    )
                    //nguồn tin
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tiêu đề
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Tóm tắt
                Text(
                    text = article.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        // Mock data để preview
        // HomeScreen(...)
    }
}