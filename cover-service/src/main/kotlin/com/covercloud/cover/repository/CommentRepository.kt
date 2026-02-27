package com.covercloud.cover.repository

import com.covercloud.cover.domain.Comment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface CommentRepository : JpaRepository<Comment, Long> {
    fun findAllByCoverId(coverId: Long): List<Comment>
    fun findAllByUserId(userId: Long): List<Comment>
    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long): List<Comment>
}