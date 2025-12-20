package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.NotificationUiModel
import com.example.myapplication.ui.components.NotificationItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    notifications: List<NotificationUiModel>,
    onBackClick: () -> Unit,
    onNotificationClick: (NotificationUiModel) -> Unit,
    onMarkAllRead: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Thông báo",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Nút đánh dấu đã đọc tất cả
                    IconButton(onClick = onMarkAllRead) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Đánh dấu tất cả đã đọc",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                )
            )
        }
    ) { innerPadding ->
        if (notifications.isEmpty()) {
            // Hiển thị Empty State nếu không có thông báo
            EmptyNotificationState(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(notifications, key = { it.id }) { item ->
                    NotificationItem(
                        notification = item,
                        onClick = { onNotificationClick(item) }
                    )
                }
            }
        }
    }
}

// Giao diện khi danh sách trống
@Composable
fun EmptyNotificationState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Bạn chưa có thông báo nào",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewNotificationScreen() {
    // Dữ liệu giả lập
    val dummyData = listOf(
        NotificationUiModel(
            id = 1,
            title = "Tin tức mới dành cho bạn!",
            body = "Giá vàng hôm nay tăng kỷ lục, người dân đổ xô đi mua...",
            timestamp = "5 phút trước",
            isRead = false,
            articleId = "123"
        ),
        NotificationUiModel(
            id = 2,
            title = "Cập nhật thị trường",
            body = "Thị trường chứng khoán có dấu hiệu hồi phục mạnh mẽ vào phiên chiều.",
            timestamp = "1 giờ trước",
            isRead = true,
            articleId = "124"
        ),
        NotificationUiModel(
            id = 3,
            title = "Công nghệ AI mới",
            body = "OpenAI vừa ra mắt phiên bản mới với khả năng xử lý ngôn ngữ vượt trội.",
            timestamp = "Hôm qua",
            isRead = true,
            articleId = "125"
        )
    )

    MaterialTheme {
        NotificationScreen(
            notifications = dummyData,
            onBackClick = { /* Quay lại */ },
            onNotificationClick = { /* Mở bài viết */ },
            onMarkAllRead = { /* Gọi API mark all read */ }
        )
    }
}