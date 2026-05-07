package com.covercloud.cover.service.dto

import com.covercloud.cover.domain.CoverGenre

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
    val coverGenre: CoverGenre?,
    val tags: List<String>,
    val link: String?,
    val originalTitle: String?,
    val originalArtist: String?,
    val originalCoverImageUrl: String?,
    val likeCount: Long,
    val viewCount: Long,
)
