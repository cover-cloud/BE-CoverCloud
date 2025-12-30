package com.covercloud.cover.service.dto

import com.covercloud.cover.domain.CoverGenre

data class CoverResponse(
    val coverId: Long,
    val musicId: Long,
    val coverTitle: String?,
    val coverArtist: String?,
    val coverGenre: CoverGenre?,
    val tags : List<String>?,
    val link: String?
)
