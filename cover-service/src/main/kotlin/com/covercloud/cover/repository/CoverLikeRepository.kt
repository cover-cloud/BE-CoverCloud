package com.covercloud.cover.repository

import com.covercloud.cover.domain.CoverLike
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface CoverLikeRepository : JpaRepository<CoverLike, Long> {
    
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
