package com.covercloud.cover.service.dto

import com.covercloud.cover.domain.CoverGenre

data class CreateServiceCoverRequest(
    val originalTitle: String,
    val originalArtist: String,
    val genre: CoverGenre?,
    val coverArtist: String?,
    val title: String?,
    val tags: List<String>?,
    val videoUrl: String?,
    val originalCoverImageUrl: String?
)