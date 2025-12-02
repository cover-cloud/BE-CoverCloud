package com.covercloud.cover.repository

import com.covercloud.cover.domain.CoverTag
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CoverTagRepository : JpaRepository<CoverTag, Long> {
    fun deleteAllByCoverId(coverId: Long)
    fun findAllByCoverId(coverId: Long): List<CoverTag>

}