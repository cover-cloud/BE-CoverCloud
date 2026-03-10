package com.covercloud.music.service.dto

data class MusicSearchRequest(
    val keyword: String,
    val limit: Int = 20,
)