package com.covercloud.cover.infrastructure.dto

data class MusicResponse(
    val id: Long,
    val title: String,
    val artist: String,
    val originalCoverImageUrl: String
)
