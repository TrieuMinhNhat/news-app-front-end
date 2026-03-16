package com.example.myapplication.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar( title = { Text( text = "Thông báo", style = MaterialTheme.typography.titleLarge.copy( fontWeight = FontWeight.Bold ) ) }, navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }, actions = { IconButton(onClick = onMarkAllRead) { Icon( Icons.Default.Check, contentDescription = "Mark all read", tint = MaterialTheme.colorScheme.primary ) } } ) }
    ) { innerPadding ->
        if(notifications.isEmpty()){
            EmptyNotificationState(modifier = Modifier.padding(innerPadding))
        }
        else{
            LazyColumn(modifier = Modifier.padding(innerPadding)) {

                items(notifications, key = { it.id }) { item ->
                    NotificationSwipeItem(
                        notification = item,
                        onClick = { onNotificationClick(item) },
                        onDelete = { onDeleteNotification(item)
                            scope.launch {
                                // Hủy snackbar cũ nếu đang hiện - chưa tối uưu
                                snackbarHostState.currentSnackbarData?.dismiss()

                                val result = snackbarHostState.showSnackbar(
                                    message = "Đã xóa thông báo",
                                    actionLabel = "Hoàn tác",
                                    duration = SnackbarDuration.Short // Khoảng 4s
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
    var isVisible by remember(notification.id) { mutableStateOf(true) }
    var pendingDelete by remember(notification.id) { mutableStateOf(false) }

    val density = LocalDensity.current

    LaunchedEffect(pendingDelete) {
        if (pendingDelete) {
            delay(300) // chờ animation
            onDelete()
        }
    }

    val dismissState = remember(notification.id) {
        SwipeToDismissBoxState(
            initialValue = SwipeToDismissBoxValue.Settled,
            density = density,
            positionalThreshold = { it * 0.4f },
            confirmValueChange = {
                if (it == SwipeToDismissBoxValue.EndToStart) {
                    isVisible = false          // animate out
                    pendingDelete = true       // trigger side-effect
                    false
                } else false
            }
        )
    }

    AnimatedVisibility(
        visible = isVisible,
        exit = shrinkVertically() + fadeOut()
    ) {
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = false,
            backgroundContent = {
                val color by animateColorAsState(
                    if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                        MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.surface,
                    label = "bg"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .background(color, shape = MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(end = 20.dp)
                    )
                }
            },
            content = {
                NotificationItem(
                    notification = notification,
                    onClick = onClick
                )
            }
        )
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
    
}