package com.covercloud.cover.repository

import com.covercloud.cover.domain.PlaylistItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PlaylistItemRepository : JpaRepository<PlaylistItem, Long> {
    fun findAllByPlaylistIdOrderByPosition(playlistId: Long): List<PlaylistItem>
    fun countByPlaylistId(playlistId: Long): Int
    fun existsByPlaylistIdAndCoverId(playlistId: Long, coverId: Long): Boolean
    fun existsByIdAndPlaylistId(id: Long, playlistId: Long): Boolean
}
