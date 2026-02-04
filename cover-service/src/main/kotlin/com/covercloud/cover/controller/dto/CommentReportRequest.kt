package com.covercloud.cover.controller.dto

data class CommentReportRequest(
    val commentId: Long,
    val reason: String,
    val description: String?
)

