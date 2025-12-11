package com.covercloud.cover.repository

import com.covercloud.cover.domain.Comment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface CommentRepository : JpaRepository<Comment, Long> {
}