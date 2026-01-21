package com.covercloud.cover.service.dto

import com.covercloud.cover.domain.CoverGenre

data class CoverListResponse(
    val coverId: Long,
    val musicId: Long?,
    val userId: Long,
    val coverArtist: String?,
    val coverTitle: String?,
    val originalArtist: String?,
    val originalTitle: String?,
    val coverGenre: CoverGenre?,
    val link: String?,
    val viewCount: Long,
    val likeCount: Long,
    val commentCount: Long,
    val tags: List<String>,
    val createdAt: String,
    val isLiked: Boolean = false
)
