package com.covercloud.cover.service

import com.covercloud.cover.domain.ReportReason
import com.covercloud.cover.repository.CoverRepository
import com.covercloud.cover.service.dto.CoverReportResponse
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CoverReportService(
    private val coverRepository: CoverRepository
) {

    @Transactional
    fun reportCover(coverId: Long, reason: String, description: String?): CoverReportResponse {
        // 커버 존재 여부 확인
        val cover = coverRepository.findById(coverId).orElseThrow { NotFoundException() }

        // 이미 신고했는지 확인
        if (cover.isReported) {
            throw IllegalArgumentException("You have already reported this cover")
        }

        // ReportReason enum으로 변환
        val reportReason = try {
            ReportReason.valueOf(reason)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid reason: $reason. Allowed values: ${ReportReason.values().joinToString(", ")}")
        }

        // 커버에 신고 정보 저장
        cover.report(reportReason, description)
        coverRepository.save(cover)

        return CoverReportResponse(
            coverId = cover.id!!,
            isReported = cover.isReported,
            reason = cover.reportReason!!,
            description = cover.reportDescription,
            reportedAt = cover.updatedAt
        )
    }

    fun getCoverReportInfo(coverId: Long): CoverReportResponse {
        val cover = coverRepository.findById(coverId).orElseThrow { NotFoundException() }

        return CoverReportResponse(
            coverId = cover.id!!,
            isReported = cover.isReported,
            reason = cover.reportReason,
            description = cover.reportDescription,
            reportedAt = cover.updatedAt
        )
    }
}
