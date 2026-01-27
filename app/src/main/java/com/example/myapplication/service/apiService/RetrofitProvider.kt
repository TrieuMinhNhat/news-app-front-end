package com.example.myapplication.service.apiService

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitProvider {

    val apiService: NewsAPIService by lazy {
        Retrofit.Builder()
            .baseUrl(NewsAPIService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsAPIService::class.java)
    }
}
