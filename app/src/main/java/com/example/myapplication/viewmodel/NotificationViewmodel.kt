package com.example.myapplication.viewmodel


import DeviceViewModel
import android.app.Application
import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.NotificationUiModel
import com.example.myapplication.data.UserPreferences
import com.example.myapplication.service.apiService.NewsAPIService
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService by lazy {
        Retrofit.Builder()
            .baseUrl(NewsAPIService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsAPIService::class.java)
    }

    private val userPrefs = UserPreferences(application)
    private val _notifications = MutableStateFlow<List<NotificationUiModel>>(emptyList())
    val notifications: StateFlow<List<NotificationUiModel>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var currentToken: String? = null

    private fun observeToken() {
        viewModelScope.launch {
            // Lắng nghe Token trực tiếp từ "Kho lưu trữ chung"
            userPrefs.fcmToken.collectLatest { token ->
                if (!token.isNullOrEmpty()) {
                    currentToken = token // Lưu lại để dùng cho các hàm markAsRead
                    loadUnreadCount(token)
                    loadNotifications(token)
                }
            }
        }
    }

    init {
        observeToken()
    }

    // Call this when screen refreshes or opens
    fun refreshData() {
        observeToken()
    }



    private suspend fun loadNotifications(token: String) {
        _isLoading.value = true
        try {
            val response = apiService.getNotifications(token)

            // Map API response to UI Model
            val uiModels = response.map { apiModel ->
                NotificationUiModel(
                    id = apiModel.id,
                    title = apiModel.title,
                    body = apiModel.body,
                    timestamp = formatTime(apiModel.createdAt),
                    isRead = apiModel.isRead,
                    articleId = apiModel.article?.toString()
                )
            }
            _notifications.value = uiModels
        } catch (e: Exception) {
            Log.e("NotifViewModel", "Error loading notifications: ${e.message}")
        } finally {
            _isLoading.value = false
        }
    }

    private suspend fun loadUnreadCount(token: String) {
        try {
            val response = apiService.getUnreadCount(token)
            _unreadCount.value = response.unreadCount
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun markAsRead(notification: NotificationUiModel) {
        viewModelScope.launch {
            val token = currentToken ?: return@launch
            // 1. Cập nhật local trước để UI mượt
            updateReadStatusLocally(notification.id)

            try {
                // 2. Gọi API
                apiService.markNotificationRead(notification.id, token)
                // 3. Tải lại số lượng chưa đọc chính xác từ server
                loadUnreadCount(token)
            } catch (e: Exception) {
                Log.e("NotifViewModel", "Error marking read: ${e.message}")
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            val token = currentToken ?: return@launch
            // Cập nhật UI ngay lập tức để tạo cảm giác mượt mà
            _notifications.value = _notifications.value.map { it.copy(isRead = true) }
            _unreadCount.value = 0

            try {
                apiService.markAllRead(token)
                // Gọi lại để đảm bảo số liệu trên server và máy đồng bộ
                loadUnreadCount(token)
            } catch (e: Exception) {
                Log.e("NotifViewModel", "Error marking all read: ${e.message}")
            }
        }
    }

    private fun updateReadStatusLocally(id: Long) {
        val currentList = _notifications.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index != -1 && !currentList[index].isRead) {
            currentList[index] = currentList[index].copy(isRead = true)
            _notifications.value = currentList
            _unreadCount.value = (_unreadCount.value - 1).coerceAtLeast(0)
        }
    }

    // Helper to format ISO date to "5 minutes ago"
    private fun formatTime(isoString: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(isoString) ?: return "Just now"

            DateUtils.getRelativeTimeSpanString(
                date.time,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            ).toString()
        } catch (e: Exception) {
            "Recent"
        }
    }
}