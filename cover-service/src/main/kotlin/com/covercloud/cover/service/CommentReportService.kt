package com.covercloud.cover.service

import com.covercloud.cover.repository.CommentRepository
import com.covercloud.cover.service.dto.CommentReportResponse
import org.springframework.data.crossstore.ChangeSetPersister
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class CommentReportService(
    private val commentRepository: CommentRepository
) {

    private val validReasons = setOf(
        "SPAM",
        "INAPPROPRIATE",
        "HARASSMENT",
        "COPYRIGHT",
        "OTHER"
    )

    @Transactional
    fun reportComment(
        commentId: Long,
        reason: String,
        description: String?
    ): CommentReportResponse {
        // 1. 유효한 신고 사유인지 확인
        if (reason !in validReasons) {
            throw IllegalArgumentException("Invalid reason: $reason. Must be one of $validReasons")
        }

        // 2. 댓글 조회
        val comment = commentRepository.findById(commentId).orElseThrow {
            ChangeSetPersister.NotFoundException()
        }

        // 3. 이미 신고된 댓글인지 확인
        if (comment.isReported) {
            throw IllegalStateException("You have already reported this comment")
        }

        // 4. 신고 정보 저장
        comment.isReported = true
        comment.reportReason = reason
        comment.reportDescription = description
        comment.reportedAt = LocalDateTime.now()

        commentRepository.save(comment)

        // 5. 응답 반환
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return CommentReportResponse(
            commentId = comment.id!!,
            isReported = comment.isReported,
            reason = comment.reportReason,
            description = comment.reportDescription,
            reportedAt = comment.reportedAt?.format(formatter)
        )
    }

    @Transactional(readOnly = true)
    fun getCommentReportInfo(commentId: Long): CommentReportResponse {
        val comment = commentRepository.findById(commentId).orElseThrow {
            ChangeSetPersister.NotFoundException()
        }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return CommentReportResponse(
            commentId = comment.id!!,
            isReported = comment.isReported,
            reason = comment.reportReason,
            description = comment.reportDescription,
            reportedAt = comment.reportedAt?.format(formatter)
        )
    }
}

