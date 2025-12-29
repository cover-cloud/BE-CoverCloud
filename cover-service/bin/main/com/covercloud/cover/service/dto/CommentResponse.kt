package com.covercloud.cover.service.dto

data class CommentResponse(
    val commentId: Long?,
    val content: String,
    val coverId: Long?,
    val userId: Long?

)
