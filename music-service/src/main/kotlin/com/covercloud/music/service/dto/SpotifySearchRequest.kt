package com.covercloud.music.service.dto

data class SpotifySearchRequest(
    val keyword: String,
    val limit: Int = 20,
)