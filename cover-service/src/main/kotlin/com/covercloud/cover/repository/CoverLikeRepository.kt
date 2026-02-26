package com.covercloud.cover.repository

import com.covercloud.cover.domain.CoverLike
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface CoverLikeRepository : JpaRepository<CoverLike, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM CoverLike cl WHERE cl.cover.id = :coverId AND cl.userId = :userId")
    fun deleteByCoverIdAndUserId(coverId: Long, userId: Long)
    fun deleteAllByCoverId(coverId: Long)
    fun findAllByCoverId(coverId: Long): List<CoverLike>
    fun existsByCoverIdAndUserId(coverId: Long, userId: Long): Boolean
    fun findAllByUserId(userId: Long): List<CoverLike>
    @Query("SELECT cl FROM CoverLike cl WHERE cl.userId = :userId ORDER BY cl.createdAt DESC")
    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long): List<CoverLike>
    fun countByCoverId(coverId: Long): Long

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
