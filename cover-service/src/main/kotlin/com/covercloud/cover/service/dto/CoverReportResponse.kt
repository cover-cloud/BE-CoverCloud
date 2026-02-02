package com.covercloud.cover.service.dto

import com.covercloud.cover.domain.ReportReason
import java.time.LocalDateTime

data class CoverReportResponse(
    val coverId: Long,
    val isReported: Boolean,
    val reason: ReportReason?,
    val description: String?,
    val reportedAt: LocalDateTime?
)
