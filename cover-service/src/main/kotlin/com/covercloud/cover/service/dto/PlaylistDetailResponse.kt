package com.covercloud.cover.service.dto

data class PlaylistDetailResponse(
    val playlistId: Long,
    val name: String,
    val itemCount: Int,
    val createdAt: String,
    val items: List<PlaylistItemResponse>?,
)

data class PlaylistItemResponse(
    val itemId: Long,
    val coverId: Long,
    val position: Int,
    val coverTitle: String?,
    val coverArtist: String?,
    val link: String?,
    val originalTitle: String?,
    val originalArtist: String?,
    val originalCoverImageUrl: String?,
    val likeCount: Long,
    val viewCount: Long,
)
