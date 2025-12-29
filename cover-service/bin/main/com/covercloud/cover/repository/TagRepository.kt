package com.covercloud.cover.repository

import com.covercloud.cover.domain.CoverTag
import com.covercloud.cover.domain.Tag
import org.springframework.data.jpa.repository.JpaRepository

interface TagRepository : JpaRepository<Tag, Long> {
    fun findByName(name: String): Tag?
}