package com.covercloud.music.service.dto

data class SpotifyTrackSummary(
    val spotifyTrackId: String,
    val title: String,
    val artist: String,
    val album: String?,
    val coverUrl: String?,
    val previewUrl: String?,
    val durationMs: Long?
)
