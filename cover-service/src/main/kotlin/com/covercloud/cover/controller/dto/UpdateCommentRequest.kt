package com.covercloud.cover.controller.dto

import com.covercloud.cover.service.dto.UpdateServiceCommentRequest

data class UpdateCommentRequest(
    val content: String,
){
    fun toDto(): UpdateServiceCommentRequest{
        return UpdateServiceCommentRequest(content)
    }
}
