package com.example.myapplication.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import com.example.myapplication.data.NotificationUiModel
import com.example.myapplication.ui.components.NotificationItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    notifications: List<NotificationUiModel>,
    onBackClick: () -> Unit,
    onNotificationClick: (NotificationUiModel) -> Unit,
    onMarkAllRead: () -> Unit,
    onDeleteNotification: (NotificationUiModel) -> Unit,
    onUndoDelete: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val hasUnread = remember(notifications) { notifications.any { !it.isRead } }
    val unreadCount = remember(notifications) { notifications.count { !it.isRead } }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Thông báo",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                actions = {
                    AnimatedVisibility(
                        visible = hasUnread,
                        enter = fadeIn(tween(200)),
                        exit = fadeOut(tween(200))
                    ) {
                        IconButton(onClick = onMarkAllRead) {
                            Icon(
                                imageVector = Icons.Outlined.DoneAll,
                                contentDescription = "Đánh dấu tất cả đã đọc",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (notifications.isEmpty()) {
            NotificationEmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 4.dp,
                    bottom = innerPadding.calculateBottomPadding() + 16.dp,
                    start = 8.dp,
                    end = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tất cả (${notifications.size})",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (unreadCount > 0) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "Chưa đọc $unreadCount",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
                items(
                    items = notifications,
                    key = { it.id }
                ) { item ->
                    NotificationSwipeItem(
                        notification = item,
                        onClick = { onNotificationClick(item) },
                        onDelete = {
                            onDeleteNotification(item)
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()

                                val result = snackbarHostState.showSnackbar(
                                    message = "Đã xóa thông báo",
                                    actionLabel = "Hoàn tác",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    onUndoDelete()
                                }
                            }
                        }
                    )
                }
            }
        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSwipeItem(
    notification: NotificationUiModel,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var isVisible by remember { mutableStateOf(true) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                isVisible = false
                true
            } else {
                false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.4f }
    )

    LaunchedEffect(isVisible) {
        if (!isVisible) {
            delay(300)
            onDelete()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(tween(200))
    ) {
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = false,
            backgroundContent = { SwipeDismissBackground(dismissState) },
            content = {
                NotificationItem(
                    notification = notification,
                    onClick = onClick
                )
            }
        )
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeDismissBackground(state: SwipeToDismissBoxState) {
    val isActive = state.targetValue == SwipeToDismissBoxValue.EndToStart
    val backgroundColor by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.errorContainer
        else MaterialTheme.colorScheme.surface,
        animationSpec = tween(200),
        label = "swipeBackground"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.onErrorContainer
        else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(200),
        label = "swipeIconTint"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .background(backgroundColor, shape = MaterialTheme.shapes.medium),
        contentAlignment = Alignment.CenterEnd
    ) {
        // Keep icon hidden until swipe is active to avoid it showing through card translucency.
        if (isActive || state.currentValue == SwipeToDismissBoxValue.EndToStart) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Xóa thông báo",
                tint = iconTint,
                modifier = Modifier.padding(end = 20.dp)
            )
        }
    }
}

@Composable
private fun NotificationEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.NotificationsNone,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outlineVariant
        )
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = "Chưa có thông báo",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = "Khi có tin tức mới phù hợp với\nchủ đề của bạn, chúng sẽ xuất hiện ở đây.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )
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
            articleId = "123",
            type = "new_article",
            keyword = null
        ),
        NotificationUiModel(
            id = 2,
            title = "Cập nhật thị trường",
            body = "Thị trường chứng khoán có dấu hiệu hồi phục mạnh mẽ vào phiên chiều.",
            timestamp = "1 giờ trước",
            isRead = true,
            articleId = "124",
            type = "new_article",
            keyword = null
        ),
        NotificationUiModel(
            id = 3,
            title = "Công nghệ AI mới",
            body = "OpenAI vừa ra mắt phiên bản mới với khả năng xử lý ngôn ngữ vượt trội.",
            timestamp = "Hôm qua",
            isRead = true,
            articleId = "125",
            type = "new_article",
            keyword = null
        )
    )

}