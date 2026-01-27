package com.example.myapplication.viewmodel


import android.app.Application
import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.NotificationUiModel
import com.example.myapplication.data.UserPreferences
import com.example.myapplication.data.repository.NotificationRepository
import com.example.myapplication.service.apiService.RetrofitProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class NotificationState(
    val items: List<NotificationUiModel> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false
)

class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = NotificationRepository(
        RetrofitProvider.apiService
    )

    private val userPrefs = UserPreferences(application)
    private var lastDeleted: NotificationUiModel? = null

    private var lastDeletedItem: NotificationUiModel? = null

    private var deleteJob: Job? = null // Job để quản lý việc đếm ngược xóa
    // 🔥 SINGLE SOURCE OF TRUTH
    private val _state = MutableStateFlow(NotificationState())
    val state: StateFlow<NotificationState> = _state.asStateFlow()

    private var currentToken: String? = null

    init {
        observeToken()
    }

    private fun observeToken() {
        viewModelScope.launch {
            userPrefs.fcmToken.collectLatest { token ->
                if (!token.isNullOrEmpty()) {
                    currentToken = token
                    syncFromServer()
                }
            }
        }
    }

    // =========================
    // 🔄 SYNC FROM SERVER (ONLY PLACE CALL GET)
    // =========================
    fun syncFromServer() {
        val token = currentToken ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                val response = repo.getNotifications(token)
                val uiModels = response.map {
                    NotificationUiModel(
                        id = it.id,
                        title = it.title,
                        body = it.body,
                        timestamp = formatTime(it.createdAt),
                        isRead = it.isRead,
                        articleId = it.article?.toString()
                    )
                }

                _state.value = NotificationState(
                    items = uiModels,
                    unreadCount = uiModels.count { !it.isRead },
                    isLoading = false
                )

            } catch (e: Exception) {
                Log.e("NotifVM", "Sync error: ${e.message}")
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    // =========================
    // 🗑 DELETE (OPTIMISTIC)
    // =========================
    fun delete(notification: NotificationUiModel) {
        // 1. Nếu đang có item chờ xóa trước đó, hãy xóa nó ngay lập tức (flush) để xử lý item mới
        if (deleteJob?.isActive == true) {
            deleteJob?.cancel() // Hủy đếm ngược cũ
            // Buộc phải xóa item cũ ngay vì user đã chuyển sang xóa item mới (tùy chọn logic)
            // Hoặc đơn giản là chấp nhận logic mỗi lần chỉ undo được 1 cái mới nhất
            lastDeletedItem?.let { commitDelete(it) }
        }

        // 2. Lưu item hiện tại để undo
        lastDeletedItem = notification

        // 3. Xóa trên UI trước (Optimistic)
        _state.update {
            it.copy(
                items = it.items.filterNot { n -> n.id == notification.id },
                unreadCount = if (!notification.isRead) (it.unreadCount - 1).coerceAtLeast(0) else it.unreadCount
            )
        }

        // 4. Bắt đầu đếm ngược 4 giây (thời gian hiển thị Snackbar)
        deleteJob = viewModelScope.launch {
            delay(4000) // Chờ 4s
            commitDelete(notification) // Nếu không ai cancel job này, thì xóa thật
            lastDeletedItem = null // Hết cơ hội undo
        }
    }


    fun undoDelete() {
        // Nếu kịp bấm Undo (Job còn đang chạy)
        if (deleteJob?.isActive == true) {
            deleteJob?.cancel() // HỦY LỆNH GỌI API

            // Khôi phục lại UI
            lastDeletedItem?.let { item ->
                _state.update {
                    val newList = (listOf(item) + it.items).sortedByDescending { n -> n.id } // Sắp xếp lại nếu cần
                    it.copy(
                        items = newList,
                        unreadCount = if (!item.isRead) it.unreadCount + 1 else it.unreadCount
                    )
                }
            }
        }
        lastDeletedItem = null
    }

    // Hàm gọi API xóa thật sự (Private)
    private fun commitDelete(item: NotificationUiModel) {
        val token = currentToken ?: return
        viewModelScope.launch {
            try {
                repo.deleteNotification(item.id, token)
                Log.d("NotifVM", "Deleted ${item.id} on server")
            } catch (e: Exception) {
                // Xử lý lỗi nếu cần (ít xảy ra), thầm lặng sync lại
                syncFromServer()
            }
        }
    }

    // =========================
    // ✅ MARK AS READ (ID)
    // =========================
    fun markAsRead(id: Long) {
        val token = currentToken ?: return

        _state.update {
            it.copy(
                items = it.items.map { n ->
                    if (n.id == id && !n.isRead) n.copy(isRead = true) else n
                },
                unreadCount = (it.unreadCount - 1).coerceAtLeast(0)
            )
        }

        viewModelScope.launch {
            try {
                repo.markRead(id, token)
            } catch (e: Exception) {
                syncFromServer()
            }
        }
    }

    // =========================
    // 🔔 MARK AS READ BY ARTICLE
    // =========================
    fun markAsReadByArticleId(articleId: String) {
        val token = currentToken ?: return

        _state.update { currentState ->
            // 1. Cập nhật trạng thái item trong list nếu tìm thấy
            val updatedItems = currentState.items.map { n ->
                if (n.articleId == articleId && !n.isRead)
                    n.copy(isRead = true)
                else n
            }

            // 2. Tính toán số lượng chưa đọc mới
            // Logic: Nếu tìm thấy item trong list thì tính lại.
            // Nếu không (cold start/list chưa load), cứ trừ đi 1 để UI cập nhật ngay.
            val itemExistsAndUnread = currentState.items.any { it.articleId == articleId && !it.isRead }

            val newUnreadCount = if (currentState.items.isNotEmpty()) {
                updatedItems.count { !it.isRead } // Nếu đã có list, tính chính xác
            } else {
                // Nếu list chưa load, tạm thời trừ 1 (nhưng không nhỏ hơn 0)
                (currentState.unreadCount - 1).coerceAtLeast(0)
            }

            currentState.copy(
                items = updatedItems,
                unreadCount = newUnreadCount
            )
        }

        viewModelScope.launch {
            try {
                // Gọi API báo server
                repo.markReadByArticle(
                    token = token,
                    body = mapOf("article_id" to articleId)
                )
                // 🔥 Quan trọng: Sau khi đánh dấu xong, nên sync lại 1 lần nữa
                // để đảm bảo số liệu chính xác tuyệt đối từ server
                syncFromServer()
            } catch (e: Exception) {
                Log.e("NotifVM", "Mark read failed", e)
                // Nếu lỗi mạng, sync lại để hoàn tác
                syncFromServer()
            }
        }
    }

    // =========================
    // ✅ MARK ALL READ
    // =========================
    fun markAllAsRead() {
        val token = currentToken ?: return

        _state.update {
            it.copy(
                items = it.items.map { n -> n.copy(isRead = true) },
                unreadCount = 0
            )
        }

        viewModelScope.launch {
            try {
                repo.markAllRead(token)
            } catch (e: Exception) {
                syncFromServer()
            }
        }
    }

    // =========================
    // ⏱ TIME FORMAT
    // =========================
    private fun formatTime(iso: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(iso) ?: return "Just now"

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
