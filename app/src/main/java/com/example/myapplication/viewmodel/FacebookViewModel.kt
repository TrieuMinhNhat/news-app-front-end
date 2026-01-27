package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.myapplication.data.repository.FacebookRepository


class FacebookViewModel: ViewModel() {
    private val repository = FacebookRepository()

    val postPager = repository
        .getFacebookPosts()
        .cachedIn(viewModelScope)
}