package com.covercloud.cover.application.dto

data class CoverResponse(
    val coverId: Long,
    val musicId: Long,
    val coverArtist: String,
    val link: String
)
