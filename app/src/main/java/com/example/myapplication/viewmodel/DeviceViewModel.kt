import android.app.Application
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import com.example.myapplication.data.DeviceRequest
import com.example.myapplication.data.UserPreferences
import com.example.myapplication.service.apiService.NewsAPIService
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.getValue


class DeviceViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService by lazy {
        Retrofit.Builder()
            .baseUrl(NewsAPIService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsAPIService::class.java)
    }
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
        // Lắng nghe Token từ UserPreferences
        viewModelScope.launch {
            userPrefs.fcmToken.collectLatest { token ->
                _fcmToken.value = token
            }
        }
    }

    private fun fetchTokenOnce() {
//        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                val token = task.result
//                _fcmToken.value = token
//                viewModelScope.launch {
//                    userPrefs.saveToken(token) // Lưu vào kho dùng chung
//                }
//                Log.d("FCM", "Token fetched and saved to DataStore")
//            }
//        }
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            viewModelScope.launch {
                userPrefs.saveToken(token)
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
        viewModelScope.launch {
            val token = fcmToken.filterNotNull().first()

            val request = DeviceRequest(
                token = token,
                device_name = Build.MODEL,
                keywords = keywords,
                topics = topics
            )

            try {
                val response = apiService.registerDevice(request)
                if (response.isSuccessful) {
                    Log.d("DeviceViewModel", "Sync success")
                }
            } catch (e: Exception) {
                Log.e("DeviceViewModel", "Sync error", e)
            }
        }
    }
}