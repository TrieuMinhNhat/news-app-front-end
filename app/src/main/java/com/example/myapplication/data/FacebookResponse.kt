package com.example.myapplication.data

import com.example.myapplication.models.FacebookPost

data class FacebookResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<FacebookPost>

)
