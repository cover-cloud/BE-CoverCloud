package com.covercloud.cover.repository

import com.covercloud.cover.domain.Playlist
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PlaylistRepository : JpaRepository<Playlist, Long> {
    fun findAllByUserId(userId: Long): List<Playlist>
    fun findByIdAndUserId(id: Long, userId: Long): Playlist?
}
