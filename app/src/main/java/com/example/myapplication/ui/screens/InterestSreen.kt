package com.example.myapplication.ui.screens

import DeviceViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.Topics
import com.example.newsapp.ui.topics.KeyWordSubscription

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterestsScreen(
    onFinishClicked: () -> Unit,
    deviceViewModel: DeviceViewModel = viewModel()
) {
    var hasChanges by remember { mutableStateOf(false) }
    // 1. Lắng nghe dữ liệu từ ViewModel (đã lưu trong máy)
    val savedTopics by deviceViewModel.savedTopics.collectAsState()
    val savedKeywords by deviceViewModel.savedKeywords.collectAsState()

    // 2. Tạo biến state tạm thời để thao tác trên màn hình này
    // Dùng launchedEffect để update state tạm thời khi dữ liệu từ ViewModel load xong
    val currentSelectedTopics = remember { mutableStateOf(savedTopics) }
    val currentKeywords = remember { mutableStateListOf<String>() }

    LaunchedEffect(savedTopics) {
        currentSelectedTopics.value = savedTopics
    }
    LaunchedEffect(savedKeywords) {
        currentKeywords.clear()
        currentKeywords.addAll(savedKeywords)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chào mừng đến với Hot news") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = {
                        // 3. Khi bấm Finish, Lưu vào ViewModel (Lưu local + Gửi Server)
                        if(hasChanges){
                            deviceViewModel.updateInterests(
                                topics = currentSelectedTopics.value.toList(),
                                keywords = currentKeywords.toList()
                            )
                        }
                        onFinishClicked()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding()
                        .height(50.dp)
                ) {
                    Text("Tiếp tục", style = MaterialTheme.typography.titleMedium)
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
                TopicChipGrid(
                    allTopics = Topics.availableTopics,
                    selectedTopicIds = currentSelectedTopics.value,
                    onTopicClicked = { topicId ->
                        val current = currentSelectedTopics.value.toMutableSet()
                        if (current.contains(topicId)) {
                            current.remove(topicId)
                        } else {
                            current.add(topicId)
                        }
                        currentSelectedTopics.value = current
                        hasChanges = true
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
                            currentKeywords.add(keyword)
                            hasChanges = true
                        }
                    },
                    onRemoveKeyword = { keyword ->
                        currentKeywords.remove(keyword)
                        hasChanges = true;
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Chủ đề quan tâm",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        // ... giữ nguyên phần còn lại của bạn
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            allTopics.forEach { topic ->
                val isSelected = selectedTopicIds.contains(topic)
                FilterChip(
                    selected = isSelected,
                    onClick = { onTopicClicked(topic) },
                    label = { Text(topic) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}