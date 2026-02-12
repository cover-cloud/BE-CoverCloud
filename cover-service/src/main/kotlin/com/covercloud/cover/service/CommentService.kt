package com.covercloud.cover.service

import com.covercloud.cover.controller.dto.UserDto
import com.covercloud.cover.domain.Comment
import com.covercloud.cover.domain.CommentLike
import com.covercloud.cover.infrastructure.feign.UserClient
import com.covercloud.cover.repository.CommentRepository
import com.covercloud.cover.repository.CommentLikeRepository
import com.covercloud.cover.repository.CoverRepository
import com.covercloud.cover.service.dto.CommentResponse
import com.covercloud.cover.service.dto.CreateServiceCommentRequest
import com.covercloud.cover.service.dto.UpdateServiceCommentRequest
import jakarta.transaction.Transactional
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val coverRepository: CoverRepository,
    private val commentLikeRepository: CommentLikeRepository,
    private val commentResponseBuilder: CommentResponseBuilder,
    private val userClient: UserClient,
    private val commentListResponseBuilder: CommentListResponseBuilder
) {
    private fun buildCommentResponse(
        comment: Comment,
        userId: Long? = null
    ): CommentResponse {
        return commentResponseBuilder.buildCommentResponse(comment, userId)
    }




    @Transactional
    fun addComment(request: CreateServiceCommentRequest, userId: Long): CommentResponse {
        val cover = coverRepository.findByIdOrNull(request.coverId)
            ?: throw NotFoundException()

        // 부모 댓글이 있으면 존재 여부 확인
        if (request.parentCommentId != null) {
            commentRepository.findByIdOrNull(request.parentCommentId)
                ?: throw NotFoundException()
        }

        val comment = Comment(
            userId = userId,
            content = request.content,
            cover = cover,
            parentCommentId = request.parentCommentId
        )

        val savedComment = commentRepository.save(comment)

        // Cover의 댓글 수 증가
        cover.commentCount += 1
        coverRepository.save(cover)

        return buildCommentResponse(savedComment, userId)
    }

    @Transactional
    fun updateComment(
        id: Long,
        request: UpdateServiceCommentRequest,
        userId: Long
    ): CommentResponse {
        val comment = commentRepository.findByIdOrNull(id)
            ?: throw NotFoundException()

        if (comment.userId != userId) {
            throw IllegalArgumentException("You can only edit your own comments")
        }

        val updatedComment = Comment(
            content = request.content,
            cover = comment.cover,
            userId = comment.userId,
            parentCommentId = comment.parentCommentId
        ).apply {
            this.id = comment.id
        }

        val savedComment = commentRepository.save(updatedComment)

        return buildCommentResponse(savedComment, userId)
    }


    @Transactional
    fun deleteComment(id: Long, userId: Long) {
        val comment = commentRepository.findByIdOrNull(id)
            ?: throw NotFoundException()
        commentLikeRepository.deleteByCommentId(id);

        // 본인 댓글인지 확인
        if (comment.userId != userId) {
            throw IllegalArgumentException("You can only delete your own comments")
        }

        val cover = comment.cover
        commentRepository.delete(comment)

        // Cover의 댓글 수 감소
        if (cover.commentCount > 0) {
            cover.commentCount -= 1
            coverRepository.save(cover)
        }
    }

    @Transactional
    fun toggleLike(commentId: Long, userId: Long): CommentResponse {
        val comment = commentRepository.findByIdOrNull(commentId)
            ?: throw NotFoundException()
        
        val existingLike = commentLikeRepository.findByCommentIdAndUserId(commentId, userId)

        if (existingLike != null) {
            // 좋아요 취소
            commentLikeRepository.delete(existingLike)
        } else {
            // 좋아요 추가
            val like = CommentLike(comment = comment, userId = userId)
            commentLikeRepository.save(like)
        }

        return buildCommentResponse(comment, userId)
    }

    fun getCommentsByCoverId(coverId: Long, userId: Long? = null): List<CommentResponse> {
        coverRepository.findByIdOrNull(coverId) ?: throw NotFoundException()

        val allComments = commentRepository.findAllByCoverId(coverId)
        if (allComments.isEmpty()) return emptyList()

        // 1. 모든 작성자 ID 추출
        val authorIds = allComments.map { it.userId }.distinct()
        val userResponse = userClient.getUsersByIds(authorIds)

        // 2. [수정] ApiResponse에서 data(리스트)를 꺼낸 후 associateBy 호출
        val userMap: Map<Long, UserDto> = userResponse.data?.associate { profile ->
            profile.userId to UserDto(
                userId = profile.userId,
                nickname = profile.nickname,
                profileImageUrl = profile.profileImageUrl,
                email = "", // UserProfileDto에 없는 필드 기본값 처리
                isDeleted = profile.isDeleted
            )
        } ?: emptyMap()

        // 3. 재귀 함수
        fun buildCommentTree(commentId: Long): List<CommentResponse> {
            return allComments
                .filter { it.parentCommentId == commentId }
                .map { reply ->
                    val response = commentListResponseBuilder.buildCommentListResponse(reply, userMap, userId)
                    response.copy(replies = buildCommentTree(reply.id!!))
                }
        }

        // 4. 부모 댓글 처리
        return allComments
            .filter { it.parentCommentId == null }
            .map { parent ->
                val response = commentListResponseBuilder.buildCommentListResponse(parent, userMap, userId)
                response.copy(replies = buildCommentTree(parent.id!!))
            }
    }


    fun getCommentsByUserId(userId: Long): List<CommentResponse> {
        return commentRepository.findAllByUserId(userId)
            .map { comment ->
                buildCommentResponse(comment, userId)
            }
    }
}