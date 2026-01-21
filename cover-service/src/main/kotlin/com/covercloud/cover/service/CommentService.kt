package com.covercloud.cover.service

import com.covercloud.cover.domain.Comment
import com.covercloud.cover.domain.CommentLike
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
    private val commentResponseBuilder: CommentResponseBuilder
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
        // Cover 존재 여부 확인
        coverRepository.findByIdOrNull(coverId)
            ?: throw NotFoundException()
        
        val allComments = commentRepository.findAllByCoverId(coverId)
        
        // 부모 댓글만 필터링 (parentCommentId가 null인 것)
        val parentComments = allComments.filter { it.parentCommentId == null }

        // 재귀적으로 대댓글 구조 생성
        fun buildCommentTree(commentId: Long): List<CommentResponse> {
            return allComments
                .filter { it.parentCommentId == commentId }
                .map { reply ->
                    commentResponseBuilder.buildCommentResponse(reply, userId).copy(
                        replies = buildCommentTree(reply.id!!)
                    )
                }
        }

        // 각 부모 댓글의 전체 트리 구성
        return parentComments.map { parent ->
            commentResponseBuilder.buildCommentResponse(parent, userId).copy(
                replies = buildCommentTree(parent.id!!)
            )
        }
    }

    fun getCommentsByUserId(userId: Long): List<CommentResponse> {
        return commentRepository.findAllByUserId(userId)
            .map { comment ->
                buildCommentResponse(comment, userId)
            }
    }
}