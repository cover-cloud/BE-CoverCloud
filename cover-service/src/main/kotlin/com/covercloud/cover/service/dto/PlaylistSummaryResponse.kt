package com.covercloud.cover.service.dto

data class PlaylistSummaryResponse(
    val playlistId: Long,
    val name: String,
    val itemCount: Int,
    val thumbnailUrl: String?,
    val createdAt: String,
)
