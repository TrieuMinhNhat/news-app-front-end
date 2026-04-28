package com.example.myapplication.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.viewmodel.NotificationViewModel

@Composable
fun NotificationRoute(
    onBack: () -> Unit,
    onNavigateToArticle: (Int) -> Unit,
    onNavigateToSocial: (String?) -> Unit,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    NotificationScreen(
        notifications = state.items,
        onBackClick = onBack,
        onNotificationClick = { notification ->
            viewModel.markAsRead(notification.id)
            when (notification.type) {
                "facebook_post_update", "threads_keyword_update" -> {
                    onNavigateToSocial(notification.keyword)
                }
                else -> {
                    notification.articleId?.toIntOrNull()?.let(onNavigateToArticle)
                }
            }
        },
        onMarkAllRead = viewModel::markAllAsRead,
        onDeleteNotification = viewModel::delete,
        onUndoDelete = viewModel::undoDelete
    )
}
