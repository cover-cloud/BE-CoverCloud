package com.covercloud.cover.repository

import com.covercloud.cover.domain.Cover
import com.covercloud.cover.domain.CoverLike
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CoverLikeRepository : JpaRepository<CoverLike, Long> {
    fun findByCoverIdAndUserId(coverId: Long, userId: Long): CoverLike?
    fun deleteByCoverIdAndUserId(coverId: Long, userId: Long)
    fun findAllByCoverId(coverId: Long): List<CoverLike>
    fun countByCoverId(coverId: Long): Long
}