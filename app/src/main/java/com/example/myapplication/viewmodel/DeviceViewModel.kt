package com.example.myapplication.viewmodel

import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.DeviceRequest
import com.example.myapplication.data.UserPreferences
import com.example.myapplication.data.repository.DeviceRepository
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceViewModel @Inject constructor(
    private val repo: DeviceRepository,
    private val userPrefs: UserPreferences
) : ViewModel() {

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
        viewModelScope.launch {
            userPrefs.fcmToken.collectLatest { token ->
                _fcmToken.value = token
            }
        }
    }

    private fun fetchTokenOnce() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            viewModelScope.launch {
                userPrefs.saveToken(token)
            }
        }
    }

    fun updateInterests(topics: List<String>, keywords: List<String>) {
        val newTopics = topics.toSet()
        val newKeywords = keywords.toSet()
        val currentTopics = savedTopics.value
        val currentKeywords = savedKeywords.value.toSet()

        if (newTopics == currentTopics && newKeywords == currentKeywords) {
            Log.d("DeviceViewModel", "No interest changes detected. Skip server sync")
            return
        }

        viewModelScope.launch {
            userPrefs.saveTopics(newTopics)
            userPrefs.saveKeywords(newKeywords)
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
                val response = repo.registerDevice(request)
                if (response.isSuccessful) {
                    Log.d("DeviceViewModel", "Sync success")
                }
            } catch (e: Exception) {
                Log.e("DeviceViewModel", "Sync error", e)
            }
        }
    }
}
