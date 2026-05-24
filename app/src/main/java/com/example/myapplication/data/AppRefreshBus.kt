package com.example.myapplication.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AppRefreshBus {
    private val _refreshAll = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1
    )

    val refreshAll = _refreshAll.asSharedFlow()

    fun refreshAllApis() {
        _refreshAll.tryEmit(Unit)
    }
}