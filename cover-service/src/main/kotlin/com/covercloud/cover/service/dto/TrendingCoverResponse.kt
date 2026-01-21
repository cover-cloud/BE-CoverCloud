package com.covercloud.cover.service.dto

import com.covercloud.cover.domain.CoverGenre

data class TrendingCoverResponse(
    val coverId: Long,
    val musicId: Long?,
    val userId: Long,
    val coverArtist: String?,
    val coverTitle: String?,
    val coverGenre: CoverGenre?,
    val link: String?,
    val currentLikeCount: Long,
    val previousLikeCount: Long,
    val likeIncrement: Long,
    val viewCount: Long,
    val commentCount: Long,
    val tags: List<String>,
    val createdAt: String,
    val isLiked: Boolean = false
)
