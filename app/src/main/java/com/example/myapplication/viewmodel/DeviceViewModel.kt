import android.app.Application
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.myapplication.data.UserPreferences
import com.example.myapplication.service.apiService.NewsAPIService.Companion.BASE_URL
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

// Chuyển sang AndroidViewModel để có Application Context dùng cho DataStore
class DeviceViewModel(application: Application) : AndroidViewModel(application) {

    private val userPrefs = UserPreferences(application)

    // StateFlow để UI lắng nghe
    private val _fcmToken = MutableStateFlow<String?>(null)
    val fcmToken: StateFlow<String?> = _fcmToken.asStateFlow()

    private val _savedTopics = MutableStateFlow<Set<String>>(emptySet())
    val savedTopics: StateFlow<Set<String>> = _savedTopics.asStateFlow()

    private val _savedKeywords = MutableStateFlow<List<String>>(emptyList())
    val savedKeywords: StateFlow<List<String>> = _savedKeywords.asStateFlow()

    init {
        loadLocalPreferences()
        fetchTokenOnce()
    }

    private fun loadLocalPreferences() {
        viewModelScope.launch {
            userPrefs.savedTopics.collectLatest {
                _savedTopics.value = it
            }
        }
        viewModelScope.launch {
            userPrefs.savedKeywords.collectLatest { set ->
                _savedKeywords.value = set.toList()
            }
        }
    }

    private fun fetchTokenOnce() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                viewModelScope.launch {
                    userPrefs.saveToken(token) // Lưu vào kho dùng chung
                }
                Log.d("FCM", "Token fetched and saved to DataStore")
            }
        }
    }

    // Hàm gọi từ UI khi user bấm nút "Finish" hoặc thay đổi setting
    fun updateInterests(topics: List<String>, keywords: List<String>) {
        viewModelScope.launch {
            // 1. Lưu Local trước (Offline first)
            userPrefs.saveTopics(topics.toSet())
            userPrefs.saveKeywords(keywords.toSet())

            // 2. Đồng bộ lên Server
            syncDeviceToServer(topics, keywords)
        }
    }

    private fun syncDeviceToServer(topics: List<String>, keywords: List<String>) {
        val token = _fcmToken.value
        if (token == null) {
            Log.e("API", "Token chưa có, không thể sync server")
            return
        }

        val json = JSONObject().apply {
            put("token", token)
            put("device_name", Build.MODEL)
            put("keywords", JSONArray(keywords))
            put("topics", JSONArray(topics))
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            "$BASE_URL/api/register_device/",
            json,
            { response -> Log.d("API", "Sync success: $response") },
            { error -> Log.e("API", "Sync error: ${error.message}") }
        )
        Volley.newRequestQueue(getApplication()).add(request)
    }
}