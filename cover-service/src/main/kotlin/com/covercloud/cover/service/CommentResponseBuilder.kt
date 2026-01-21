package com.covercloud.cover.service

import com.covercloud.cover.domain.Comment
import com.covercloud.cover.infrastructure.feign.UserClient
import com.covercloud.cover.repository.CommentLikeRepository
import com.covercloud.cover.service.dto.CommentResponse
import java.time.format.DateTimeFormatter
import org.springframework.stereotype.Component

@Component
class CommentResponseBuilder(
    private val commentLikeRepository: CommentLikeRepository,
    private val userClient: UserClient
) {
    fun buildCommentResponse(
        comment: Comment,
        userId: Long? = null
    ): CommentResponse {
        val likeCount = commentLikeRepository.countByCommentId(comment.id!!)
        val isLiked = if (userId != null) {
            commentLikeRepository.existsByCommentIdAndUserId(comment.id!!, userId)
        } else {
            false
        }

        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        // User 정보 조회
        var nickname = ""
        var profileImageUrl: String? = null

        try {
            val userProfile = userClient.getUserProfile(comment.userId)
            if (userProfile.success && userProfile.data != null) {
                nickname = userProfile.data!!.nickname
                profileImageUrl = userProfile.data!!.profileImageUrl
            }
        } catch (e: Exception) {
            // User 정보 조회 실패 시 무시
        }

        return CommentResponse(
            commentId = comment.id,
            content = comment.content,
            coverId = comment.cover.id,
            userId = comment.userId,
            parentCommentId = comment.parentCommentId,
            likeCount = likeCount,
            isLiked = isLiked,
            createdAt = comment.createdAt?.format(dateFormatter) ?: "",
            nickname = nickname,
            profileImageUrl = profileImageUrl
        )
    }
}

