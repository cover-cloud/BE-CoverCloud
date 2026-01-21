package com.covercloud.cover.repository

import com.covercloud.cover.domain.CommentLike
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CommentLikeRepository : JpaRepository<CommentLike, Long> {
    fun findByCommentIdAndUserId(commentId: Long, userId: Long): CommentLike?
    fun deleteByCommentIdAndUserId(commentId: Long, userId: Long)
    fun countByCommentId(commentId: Long): Long
    fun existsByCommentIdAndUserId(commentId: Long, userId: Long): Boolean
}

