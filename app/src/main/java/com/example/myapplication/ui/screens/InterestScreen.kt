package com.example.myapplication.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.Topics
import com.example.myapplication.viewmodel.DeviceViewModel
import com.example.newsapp.ui.topics.KeyWordSubscription
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterestsScreen(
    onFinishClicked: () -> Unit,
    deviceViewModel: DeviceViewModel = hiltViewModel()
) {
    val savedTopics by deviceViewModel.savedTopics.collectAsStateWithLifecycle()
    val savedKeywords by deviceViewModel.savedKeywords.collectAsStateWithLifecycle()

    fun normalizeKeywords(keywords: Collection<String>): List<String> {
        return keywords
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase(Locale.ROOT) }
    }

    val normalizedSavedKeywords = remember(savedKeywords) {
        normalizeKeywords(savedKeywords)
    }

    var baselineTopics by remember { mutableStateOf<Set<String>?>(null) }
    var baselineKeywords by remember { mutableStateOf<List<String>?>(null) }

    var currentSelectedTopics by remember { mutableStateOf<Set<String>>(emptySet()) }
    val currentKeywords = remember { mutableStateListOf<String>() }

    var showUnsavedChangesDialog by remember { mutableStateOf(false) }

    LaunchedEffect(savedTopics, normalizedSavedKeywords) {
        val currentBaselineTopics = baselineTopics
        val currentBaselineKeywords = baselineKeywords

        val hasUserEdited =
            currentBaselineTopics != null &&
                currentBaselineKeywords != null &&
                (
                    currentSelectedTopics != currentBaselineTopics ||
                        normalizeKeywords(currentKeywords) != currentBaselineKeywords
                )

        if (!hasUserEdited) {
            baselineTopics = savedTopics
            baselineKeywords = normalizedSavedKeywords

            currentSelectedTopics = savedTopics
            currentKeywords.clear()
            currentKeywords.addAll(normalizedSavedKeywords)
        }
    }

    val isDirty by remember {
        derivedStateOf {
            val topicsBaseline = baselineTopics
            val keywordsBaseline = baselineKeywords

            if (topicsBaseline == null || keywordsBaseline == null) {
                false
            } else {
                currentSelectedTopics != topicsBaseline ||
                    normalizeKeywords(currentKeywords) != keywordsBaseline
            }
        }
    }

    fun saveAndExit() {
        deviceViewModel.updateInterests(
            topics = currentSelectedTopics.toList(),
            keywords = normalizeKeywords(currentKeywords),
            onComplete = onFinishClicked
        )
    }

    fun requestExit() {
        if (isDirty) {
            showUnsavedChangesDialog = true
        } else {
            onFinishClicked()
        }
    }

    BackHandler {
        requestExit()
    }

    if (showUnsavedChangesDialog) {
        AlertDialog(
            onDismissRequest = {
                showUnsavedChangesDialog = false
            },
            title = {
                Text("Có thay đổi chưa lưu")
            },
            text = {
                Text("Lưu thay đổi trước khi rời khỏi màn hình?")
            },
            shape = MaterialTheme.shapes.extraLarge,
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            confirmButton = {
                TextButton(
                    onClick = {
                        showUnsavedChangesDialog = false
                        saveAndExit()
                    }
                ) {
                    Text("Lưu")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            showUnsavedChangesDialog = false
                            onFinishClicked()
                        }
                    ) {
                        Text("Không lưu")
                    }

                    TextButton(
                        onClick = {
                            showUnsavedChangesDialog = false
                        }
                    ) {
                        Text("Ở lại")
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Cá nhân hóa",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tùy chỉnh bản tin của bạn",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { requestExit() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
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
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = {
                        if (isDirty) {
                            saveAndExit()
                        } else {
                            onFinishClicked()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    // Icon(
                    //     imageVector = Icons.Default.Check,
                    //     contentDescription = null,
                    //     modifier = Modifier.size(20.dp)
                    // )

                    // Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (isDirty) "Lưu thay đổi" else "Tiếp tục",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(32.dp)
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
                        currentSelectedTopics =
                            if (currentSelectedTopics.contains(topicId)) {
                                currentSelectedTopics - topicId
                            } else {
                                currentSelectedTopics + topicId
                            }
                    },
                    onClearAll = {
                        currentSelectedTopics = emptySet()
                    },
                    maxInitialDisplay = 9
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Theo dõi từ khóa",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Nhận thông báo khi có bài viết chứa từ khóa này.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    KeyWordSubscription(
                        keywords = currentKeywords,
                        onAddKeyword = { rawKeyword ->
                            val keyword = rawKeyword.trim()

                            val alreadyExists = currentKeywords.any {
                                it.equals(keyword, ignoreCase = true)
                            }

                            if (keyword.isNotBlank() && !alreadyExists) {
                                currentKeywords.add(keyword)
                            }
                        },
                        onRemoveKeyword = { keyword ->
                            currentKeywords.remove(keyword)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TopicChipGrid(
    allTopics: List<String>,
    selectedTopicIds: Set<String>,
    onTopicClicked: (String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Chủ đề quan tâm",
    maxInitialDisplay: Int = 15,
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedCount > 0) "$title ($selectedCount)" else title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (showTrending && trendingTopics.isNotEmpty()) {
            TrendingTopicsRow(
                trendingTopics = trendingTopics,
                selectedTopicIds = selectedTopicIds,
                onTopicClicked = onTopicClicked
            )
        }

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
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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
                    } else {
                        null
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = if (isSelected) {
                        null
                    } else {
                        FilterChipDefaults.filterChipBorder(
                            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            selected = false,
                            enabled = true
                        )
                    },
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

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
                                "Thu gọn"
                            } else {
                                "Xem thêm $remainingCount chủ đề"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Icon(
                            imageVector = if (isExpanded) {
                                Icons.Default.ArrowDropUp
                            } else {
                                Icons.Default.ArrowDropDown
                            },
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        if (selectedCount > 0) {
            SelectedTopicsSummary(
                selectedTopics = selectedTopicIds,
                onTopicClicked = onTopicClicked,
                onClearAll = onClearAll
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
                text = "Đang thịnh hành",
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
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        labelColor = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
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
                text = "Đã chọn",
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
                            contentDescription = "Đã chọn",
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
