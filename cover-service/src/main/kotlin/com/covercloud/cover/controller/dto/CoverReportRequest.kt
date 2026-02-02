package com.covercloud.cover.controller.dto

data class CoverReportRequest(
    val coverId: Long,
    val reason: String,  // ReportReason enum 값 (예: "INAPPROPRIATE_CONTENT")
    val description: String? = null
)
