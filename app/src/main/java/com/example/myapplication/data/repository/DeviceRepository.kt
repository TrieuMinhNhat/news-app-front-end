package com.example.myapplication.data.repository

import com.example.myapplication.data.DeviceRequest
import com.example.myapplication.service.apiService.NewsAPIService

class DeviceRepository(
    private val api: NewsAPIService
) {
    suspend fun registerDevice(request: DeviceRequest) =
        api.registerDevice(request)
}