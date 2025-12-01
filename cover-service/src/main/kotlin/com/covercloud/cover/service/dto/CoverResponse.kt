package com.covercloud.cover.service.dto

data class CoverResponse(
    val coverId: Long,
    val musicId: Long,
    val coverArtist: String,
    val coverTitle: String,
    val coverGenre: String,
    val link: String
)
