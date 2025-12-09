package com.covercloud.cover.service.dto

data class CoverListResponse(
    val coverId: Long,
    val musicId: Long,
    val userId: Long,
    val coverArtist: String?,
    val coverTitle: String?,
    val coverGenre: String?,
    val link: String?,
    val viewCount: Long,
    val likeCount: Long,
    val commentCount: Long,
    val tags: List<String>,
    val createdAt: String
)
