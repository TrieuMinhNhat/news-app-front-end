package com.example.myapplication.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.MainEvent
import com.example.myapplication.viewmodel.NotificationViewModel

@Composable
fun NotificationRoute(
    // Bỏ tham số mainEvent ở đây vì không dùng nữa
    onBack: () -> Unit,
    onNavigateToArticle: (Int) -> Unit,
    viewModel: NotificationViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // ❌ XÓA hoặc COMMENT LẠI khối LaunchedEffect(mainEvent.value) cũ ở đây
    // Vì nó đã được chuyển sang AppNavGraph

    NotificationScreen(
        notifications = state.items,
        onBackClick = onBack,
        onNotificationClick = { notification ->
            // Logic xử lý khi click vào item trong danh sách
            viewModel.markAsRead(notification.id)
            notification.articleId?.toIntOrNull()?.let(onNavigateToArticle)
        },
        onMarkAllRead = viewModel::markAllAsRead,
        onDeleteNotification = viewModel::delete,
        onUndoDelete = viewModel::undoDelete
    )
}
