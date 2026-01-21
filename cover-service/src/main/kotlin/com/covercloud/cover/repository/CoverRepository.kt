package com.covercloud.cover.repository

import com.covercloud.cover.domain.Cover
import com.covercloud.cover.domain.CoverGenre
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CoverRepository : JpaRepository<Cover, Long> {
	fun findAllByUserId(userId: Long, pageable: Pageable): Page<Cover>
	fun findAllByCoverGenre(genre: CoverGenre, pageable: Pageable): Page<Cover>

	@Query("SELECT c FROM Cover c WHERE LOWER(c.coverTitle) LIKE LOWER(CONCAT('%', :title, '%'))")
	fun searchByTitle(
		@Param("title") title: String,
		pageable: Pageable
	): Page<Cover>

	@Query("SELECT DISTINCT c FROM Cover c INNER JOIN CoverTag ct ON c = ct.cover WHERE LOWER(ct.tag.name) LIKE LOWER(CONCAT('%', :tags, '%'))")
	fun searchByTags(
		@Param("tags") tags: String,
		pageable: Pageable
	): Page<Cover>
}