package com.covercloud.cover.service

import com.covercloud.cover.controller.dto.UserDto
import com.covercloud.cover.domain.Comment
import com.covercloud.cover.repository.CommentLikeRepository
import com.covercloud.cover.service.dto.CommentResponse
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter

@Component
class CommentListResponseBuilder(
    private val commentLikeRepository: CommentLikeRepository
    // userClient 의존성 제거 (Service에서 일괄 처리 권장)
) {
    fun buildCommentListResponse(
        comment: Comment,
        userMap: Map<Long, UserDto>, // 미리 조회한 유저 맵 전달
        userId: Long? = null
    ): CommentResponse {
        val likeCount = commentLikeRepository.countByCommentId(comment.id!!)
        val isLiked = userId?.let { commentLikeRepository.existsByCommentIdAndUserId(comment.id!!, it) } ?: false

        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        // 맵에서 유저 정보 찾기 (API 호출 X)
        val userProfile = userMap[comment.userId]

        return CommentResponse(
            commentId = comment.id,
            content = comment.content,
            coverId = comment.cover.id,
            userId = comment.userId,
            parentCommentId = comment.parentCommentId,
            likeCount = likeCount,
            isLiked = isLiked,
            createdAt = comment.createdAt?.format(dateFormatter) ?: "",
            nickname = userProfile?.nickname ?: "알 수 없는 사용자", // 기본값 설정
            profileImageUrl = userProfile?.profileImageUrl,
            replies = emptyList() // 기본값
        )
    }
}