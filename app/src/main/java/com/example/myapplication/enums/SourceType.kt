package com.example.myapplication.enums

import com.google.gson.annotations.SerializedName

enum class SourceType(val value: String) {
    @SerializedName("fb")
    FACEBOOK("fb"),

    @SerializedName("thr")
    THREADS("thr")
}
