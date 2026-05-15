package com.example.myapplication.data

import com.example.myapplication.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiBaseUrlProvider @Inject constructor(
    userPreferences: UserPreferences,
    appScope: CoroutineScope
) {
    private val _current = MutableStateFlow(BuildConfig.API_BASE_URL)
    val current: StateFlow<String> = _current

    init {
        appScope.launch {
            userPreferences.apiBaseUrl.collect { _current.value = it }
        }
    }
}
