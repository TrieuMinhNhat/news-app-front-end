package com.example.myapplication.worker


import android.content.Context
import android.os.Build
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplication.data.DeviceRequest
import com.example.myapplication.data.UserPreferences
import com.example.myapplication.service.apiService.NewsAPIService
import kotlinx.coroutines.flow.first
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TokenWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val token = inputData.getString("token") ?: return Result.failure()

        // Lấy topic/keyword từ Local DataStore
        val userPrefs = UserPreferences(applicationContext)
        val topics = userPrefs.savedTopics.first().toList()
        val keywords = userPrefs.savedKeywords.first().toList()

        // Setup Retrofit (tạo mới instance vì Worker chạy tiến trình riêng)
        val apiService = Retrofit.Builder()
            .baseUrl(NewsAPIService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsAPIService::class.java)

        return try {
            val request = DeviceRequest(
                token = token,
                device_name = Build.MODEL,
                keywords = keywords,
                topics = topics
            )
            val response = apiService.registerDevice(request)
            if (response.isSuccessful) Result.success() else Result.retry()
        } catch (e: Exception) {
            Log.e("TokenWorker", "Error sending token", e)
            Result.retry() // Tự động thử lại nếu mất mạng
        }
    }
}