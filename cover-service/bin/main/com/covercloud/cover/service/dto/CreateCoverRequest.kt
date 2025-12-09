package com.covercloud.cover.service.dto

data class CreateCoverRequest(
    val originalTitle: String,
    val originalArtist: String,
    val genre: String?,
    val coverArtist: String?,
    val title: String?,
    val tags: List<String>?,
    val videoUrl: String?
)