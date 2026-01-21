package com.covercloud.cover.repository

import com.covercloud.cover.domain.CoverLike
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import com.covercloud.cover.domain.Cover

@Repository
interface CoverLikeRepository : JpaRepository<CoverLike, Long> {
    fun findByCoverIdAndUserId(coverId: Long, userId: Long): CoverLike?
    fun deleteByCoverIdAndUserId(coverId: Long, userId: Long)
    fun findAllByCoverId(coverId: Long): List<CoverLike>
    fun countByCoverId(coverId: Long): Long
    fun existsByCoverIdAndUserId(coverId: Long, userId: Long): Boolean

    @Query("""
        SELECT cl.cover.id, COUNT(cl.id)
        FROM CoverLike cl
        WHERE cl.createdAt >= :startDate
        GROUP BY cl.cover.id
    """)
    fun countLikesByPeriod(
        @Param("startDate") startDate: LocalDateTime
    ): List<Array<Any>>
}
