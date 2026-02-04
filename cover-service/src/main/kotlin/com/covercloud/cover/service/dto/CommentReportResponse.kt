package com.covercloud.cover.service.dto

data class CommentReportResponse(
    val commentId: Long,
    val isReported: Boolean,
    val reason: String?,
    val description: String?,
    val reportedAt: String?
)

