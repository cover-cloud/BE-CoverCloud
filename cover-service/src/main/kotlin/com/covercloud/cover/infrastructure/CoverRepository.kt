package com.covercloud.cover.infrastructure

import com.covercloud.cover.domain.Cover
import org.springframework.data.jpa.repository.JpaRepository

interface CoverRepository : JpaRepository<Cover, Long> {
}