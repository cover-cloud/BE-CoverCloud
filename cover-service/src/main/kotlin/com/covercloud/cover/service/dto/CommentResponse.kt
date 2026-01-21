package com.covercloud.cover.service.dto

data class CommentResponse(
    val commentId: Long?,
    val content: String,
    val coverId: Long?,
    val userId: Long?,
    val parentCommentId: Long? = null,
    val replies: List<CommentResponse>? = null,
    val likeCount: Long = 0,
    val isLiked: Boolean = false,
    val createdAt: String = "",
    val nickname: String = "",
    val profileImageUrl: String? = null
)
