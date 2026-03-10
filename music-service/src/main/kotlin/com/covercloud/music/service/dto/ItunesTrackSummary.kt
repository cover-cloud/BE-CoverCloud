package com.covercloud.music.service.dto

data class ItunesTrackSummary(
    val itunesTrackId: String,
    val title: String,
    val artist: String,
    val album: String?,
    val coverUrl: String?,
    val previewUrl: String?,
    val durationMs: Long?
)
