package com.example.myapplication.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.myapplication.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// Tạo DataStore instance (Singleton)
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        val KEY_TOPICS = stringSetPreferencesKey("saved_topics")
        val KEY_KEYWORDS = stringSetPreferencesKey("saved_keywords")
        val KEY_TOKEN = stringPreferencesKey("fcm_token")
        val KEY_API_BASE_URL = stringPreferencesKey("api_base_url")
    }

    // Lấy danh sách Topics đã lưu (Flow giúp tự động cập nhật UI)
    val savedTopics: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_TOPICS] ?: emptySet()
        }

    // Lấy danh sách Keywords đã lưu
    val savedKeywords: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_KEYWORDS] ?: emptySet()
        }

    // Lưu Topics
    suspend fun saveTopics(topics: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[KEY_TOPICS] = topics
        }
    }

    // Lưu Keywords
    suspend fun saveKeywords(keywords: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[KEY_KEYWORDS] = keywords
        }
    }

    // Lấy Token (Flow)
    val fcmToken: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_TOKEN]
        }

    // Lấy API Base URL (Flow)
    val apiBaseUrl: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_API_BASE_URL] ?: BuildConfig.API_BASE_URL
        }

    // Hàm lưu Token
    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_TOKEN] = token
        }
    }

    suspend fun saveApiBaseUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_API_BASE_URL] = url
        }
    }

    suspend fun getApiBaseUrl(): String = apiBaseUrl.first()
}