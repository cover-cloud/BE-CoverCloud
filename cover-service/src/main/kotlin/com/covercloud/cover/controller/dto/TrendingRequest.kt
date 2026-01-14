package com.covercloud.cover.controller.dto

data class TrendingRequest(
    val genres: List<String>? = null,
    val period: String? = null,
    val page: Int = 0,
    val size: Int = 20
)
