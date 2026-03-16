package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FiberNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicBar(
    savedTopics: List<String>,
    savedKeywords: List<String>,
    selectedTopic: String?,
    isInterestMode: Boolean,
    onTopicSelected: (String) -> Unit,
    onInterestSelected: () -> Unit,
    onAddTopicClicked: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
//        tonalElevation = 3.dp,
//        shadowElevation = 2.dp
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
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