package com.covercloud.cover.repository

import com.covercloud.cover.domain.Cover
import com.covercloud.cover.domain.CoverGenre
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CoverRepository : JpaRepository<Cover, Long> {
	fun findAllByUserId(userId: Long, pageable: Pageable): Page<Cover>
	fun findAllByCoverGenre(genre: CoverGenre, pageable: Pageable): Page<Cover>
}