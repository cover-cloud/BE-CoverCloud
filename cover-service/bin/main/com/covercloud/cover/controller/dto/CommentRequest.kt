package com.covercloud.cover.controller.dto

import com.covercloud.cover.service.dto.CreateServiceCommentRequest

data class CommentRequest(
    val content: String,
    val coverId: Long,
    val userId: Long,
){
    fun toDto(): CreateServiceCommentRequest{
        return CreateServiceCommentRequest(
            content = content,
            coverId = coverId,
            userId = userId,
        )
    }
}
