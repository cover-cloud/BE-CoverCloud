package com.covercloud.cover.service.dto

data class CreateServiceCommentRequest(
    val content: String,
    val coverId: Long?,
    val userId: Long?
)
