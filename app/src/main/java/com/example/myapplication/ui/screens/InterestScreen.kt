package com.example.myapplication.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.data.Topics
import com.example.myapplication.ui.screens.TopicChipGrid
import com.example.myapplication.viewmodel.DeviceViewModel
import com.example.newsapp.ui.topics.KeyWordSubscription

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterestsScreen(
    onFinishClicked: () -> Unit,
    deviceViewModel: DeviceViewModel = hiltViewModel()
) {
    // 1. Lắng nghe dữ liệu từ ViewModel (đã lưu trong máy)
    val savedTopics by deviceViewModel.savedTopics.collectAsState()
    val savedKeywords by deviceViewModel.savedKeywords.collectAsState()

    var currentSelectedTopics by remember { mutableStateOf(savedTopics) }
    val currentKeywords = remember { mutableStateListOf(*savedKeywords.toTypedArray()) }
    var hasUserEdited by remember { mutableStateOf(false) }

    LaunchedEffect(savedTopics, savedKeywords) {
        if (!hasUserEdited) {
            currentSelectedTopics = savedTopics
            currentKeywords.clear()
            currentKeywords.addAll(savedKeywords)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chào mừng đến với Hot News") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp // Bóng đổ
            ) {
                Button(
                    onClick = {
                        // ViewModel tự kiểm tra có thay đổi hay không trước khi sync server
                        deviceViewModel.updateInterests(
                            topics = currentSelectedTopics.toList(),
                            keywords = currentKeywords.toList()
                        )
                        onFinishClicked()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding() // Rất tốt, tránh bị đè bởi thanh điều hướng hệ thống
                        .height(54.dp), // Nên để nút cao hơn 1 xíu (chuẩn là 48dp - 56dp)
                    shape = MaterialTheme.shapes.large
                ) {
                    Text("Tiếp tục", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Cá nhân hóa bản tin",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Chọn chủ đề và từ khóa để ưu tiên nội dung phù hợp nhất.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item {
                TopicChipGrid(
                    allTopics = Topics.availableTopics,
                    selectedTopicIds = currentSelectedTopics,
                    onTopicClicked = { topicId ->
                        hasUserEdited = true
                        currentSelectedTopics = if (currentSelectedTopics.contains(topicId)) {
                            currentSelectedTopics - topicId // Tạo Set mới bỏ phần tử này đi
                        } else {
                            currentSelectedTopics + topicId // Tạo Set mới thêm phần tử này vào
                        }
                    }
                )
            }
            item {
                Text(
                    text = "Theo dõi từ khóa",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Nhận thông báo khi có bài viết chứa từ khóa này.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                KeyWordSubscription(
                    keywords = currentKeywords,
                    onAddKeyword = { keyword ->
                        if (keyword.isNotBlank() && !currentKeywords.contains(keyword)) {
                            hasUserEdited = true
                            currentKeywords.add(keyword)
                        }
                    },
                    onRemoveKeyword = { keyword ->
                        hasUserEdited = true
                        currentKeywords.remove(keyword)
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}
// Giữ nguyên TopicChipGrid ở dưới...
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TopicChipGrid(
    allTopics: List<String>,
    selectedTopicIds: Set<String>,
    onTopicClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Chủ đề quan tâm",
    maxInitialDisplay: Int = 15,
    showCount: Boolean = true,
    showTrending: Boolean = false,
    trendingTopics: Set<String> = emptySet()
) {
    var isExpanded by remember { mutableStateOf(false) }
    val selectedCount = selectedTopicIds.size

    val displayedTopics = if (isExpanded) allTopics else allTopics.take(maxInitialDisplay)
    val hasMoreTopics = allTopics.size > maxInitialDisplay
    val remainingCount = allTopics.size - maxInitialDisplay

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with title and selection summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
//            if (showCount) {
//                Surface(
//                    color = MaterialTheme.colorScheme.primaryContainer,
//                    shape = RoundedCornerShape(999.dp)
//                ) {
//                    Text(
//                        text = "Đã chọn $selectedCount",
//                        style = MaterialTheme.typography.labelLarge,
//                        color = MaterialTheme.colorScheme.onPrimaryContainer,
//                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
//                    )
//                }
//            }
        }

        // Trending topics row (optional)
        if (showTrending && trendingTopics.isNotEmpty()) {
            TrendingTopicsRow(
                trendingTopics = trendingTopics,
                selectedTopicIds = selectedTopicIds,
                onTopicClicked = onTopicClicked
            )
        }

        // Main topic chips
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Topics Flow
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    displayedTopics.forEach { topic ->
                        val isSelected = selectedTopicIds.contains(topic)
                        val isTrending = trendingTopics.contains(topic)

                        FilterChip(
                            selected = isSelected,
                            onClick = { onTopicClicked(topic) },
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = topic,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (isTrending) {
                                        Icon(
                                            imageVector = Icons.Default.TrendingUp,
                                            contentDescription = "Trending",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Đã chọn",
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = if (isSelected) null else FilterChipDefaults.filterChipBorder(
                                borderColor = MaterialTheme.colorScheme.outlineVariant,
                                selected = false,
                                enabled = true
                            )
                        )
                    }
                }

                // Show more/less button
                if (hasMoreTopics) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(
                            onClick = { isExpanded = !isExpanded },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (isExpanded) {
                                        "Thu nhỏ"
                                    } else {
                                        "Xem thêm ${remainingCount} chủ đề khác"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = if (isExpanded) "Show less" else "Show more",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Selection summary (optional)
        if (selectedCount > 0) {
            SelectedTopicsSummary(
                selectedTopics = selectedTopicIds,
                allTopics = allTopics,
                onTopicClicked = onTopicClicked,
                onClearAll = { /* Clear all selection */
                    selectedTopicIds.toList().forEach { topic ->
                        onTopicClicked(topic)
                    }
                }
            )
        }
    }
}

@Composable
fun TrendingTopicsRow(
    trendingTopics: Set<String>,
    selectedTopicIds: Set<String>,
    onTopicClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Trending now",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(trendingTopics.toList()) { topic ->
                val isSelected = selectedTopicIds.contains(topic)
                AssistChip(
                    onClick = { onTopicClicked(topic) },
                    label = { Text(topic) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            modifier = Modifier.size(AssistChipDefaults.IconSize)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = if (isSelected)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = AssistChipDefaults.assistChipBorder(
                        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        enabled = true
                    )
                )
            }
        }
    }
}

@Composable
fun SelectedTopicsSummary(
    selectedTopics: Set<String>,
    allTopics: List<String>,
    onTopicClicked: (String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Đã chọn (${selectedTopics.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TextButton(
                onClick = onClearAll,
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Text(
                    text = "Xóa tất cả",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(selectedTopics.toList()) { topic ->
                SuggestionChip(
                    onClick = { onTopicClicked(topic) },
                    label = { Text(topic) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            modifier = Modifier.size(SuggestionChipDefaults.IconSize)
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}
