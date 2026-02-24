package com.covercloud.music.service.dto

data class MusicResponse(
    val id: Long,
    val title: String,
    val artist: String,
    val originalCoverImageUrl: String
)
