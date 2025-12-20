package com.example.myapplication.models

data class DjangoPage (val count: Int,
                       val next: String?,
                       val previous: String?,
                       val results: List<Article>)

