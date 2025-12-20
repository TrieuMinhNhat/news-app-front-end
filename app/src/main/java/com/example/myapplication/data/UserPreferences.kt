package com.example.myapplication.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Tạo DataStore instance (Singleton)
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserPreferences(private val context: Context) {

    companion object {
        val KEY_TOPICS = stringSetPreferencesKey("saved_topics")
        val KEY_KEYWORDS = stringSetPreferencesKey("saved_keywords")
        val KEY_TOKEN = stringPreferencesKey("fcm_token")
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

    // Hàm lưu Token
    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_TOKEN] = token
        }
    }
}