package com.example.myapplication.data

data class DeviceRequest(
    val token: String,
    val device_name: String,
    val keywords: List<String>,
    val topics: List<String>
)