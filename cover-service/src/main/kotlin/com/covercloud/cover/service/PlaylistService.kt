package com.covercloud.cover.service

import com.covercloud.cover.domain.Playlist
import com.covercloud.cover.domain.PlaylistItem
import com.covercloud.cover.infrastructure.feign.MusicClient
import com.covercloud.cover.repository.CoverRepository
import com.covercloud.cover.repository.CoverTagRepository
import com.covercloud.cover.repository.PlaylistItemRepository
import com.covercloud.cover.repository.PlaylistRepository
import com.covercloud.cover.service.dto.PlaylistDetailResponse
import com.covercloud.cover.service.dto.PlaylistItemResponse
import com.covercloud.cover.service.dto.PlaylistSummaryResponse
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter

@Service
class PlaylistService(
    private val playlistRepository: PlaylistRepository,
    private val playlistItemRepository: PlaylistItemRepository,
    private val coverRepository: CoverRepository,
    private val coverTagRepository: CoverTagRepository,
    private val musicClient: MusicClient,
) {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun getMyPlaylists(userId: Long): List<PlaylistSummaryResponse> {
        return playlistRepository.findAllByUserId(userId).map { playlist ->
            val itemCount = playlistItemRepository.countByPlaylistId(playlist.id!!)
            val firstItem = playlistItemRepository.findAllByPlaylistIdOrderByPosition(playlist.id!!).firstOrNull()
            val firstCover = firstItem?.let { coverRepository.findByIdOrNull(it.coverId) }
            val thumbnailUrl = firstCover?.let { getThumbnailUrl(it.musicId) }
            PlaylistSummaryResponse(
                playlistId = playlist.id!!,
                name = playlist.name,
                itemCount = itemCount,
                thumbnailUrl = thumbnailUrl,
                link = firstCover?.link,
                createdAt = playlist.createdAt.format(formatter),
            )
        }
    }

    @Transactional
    fun getPlaylist(playlistId: Long, includeItems: Boolean): PlaylistDetailResponse {
        val playlist = playlistRepository.findByIdOrNull(playlistId)
            ?: throw NoSuchElementException("Playlist not found")

        val itemCount = playlistItemRepository.countByPlaylistId(playlistId)
        val items = if (includeItems) {
            playlistItemRepository.findAllByPlaylistIdOrderByPosition(playlistId)
                .map { buildItemResponse(it) }
        } else {
            null
        }

        return PlaylistDetailResponse(
            playlistId = playlist.id!!,
            name = playlist.name,
            itemCount = itemCount,
            createdAt = playlist.createdAt.format(formatter),
            items = items,
        )
    }

    @Transactional
    fun createPlaylist(userId: Long, name: String): PlaylistSummaryResponse {
        val playlist = playlistRepository.save(Playlist(userId = userId, name = name))
        return PlaylistSummaryResponse(
            playlistId = playlist.id!!,
            name = playlist.name,
            itemCount = 0,
            thumbnailUrl = null,
            link = null,
            createdAt = playlist.createdAt.format(formatter),
        )
    }

    @Transactional
    fun updatePlaylistName(playlistId: Long, userId: Long, name: String) {
        val playlist = playlistRepository.findByIdAndUserId(playlistId, userId)
            ?: throw NoSuchElementException("Playlist not found or access denied")
        playlist.name = name
    }

    @Transactional
    fun deletePlaylist(playlistId: Long, userId: Long) {
        val playlist = playlistRepository.findByIdAndUserId(playlistId, userId)
            ?: throw NoSuchElementException("Playlist not found or access denied")
        playlistItemRepository.deleteAll(playlistItemRepository.findAllByPlaylistIdOrderByPosition(playlistId))
        playlistRepository.delete(playlist)
    }

    @Transactional
    fun addItem(playlistId: Long, userId: Long, coverId: Long): PlaylistItemResponse {
        val playlist = playlistRepository.findByIdAndUserId(playlistId, userId)
            ?: throw NoSuchElementException("Playlist not found or access denied")

        coverRepository.findByIdOrNull(coverId)
            ?: throw NoSuchElementException("Cover not found")

        if (playlistItemRepository.existsByPlaylistIdAndCoverId(playlistId, coverId)) {
            throw IllegalStateException("Cover already in playlist")
        }

        val nextPosition = playlistItemRepository.countByPlaylistId(playlistId)
        val item = playlistItemRepository.save(PlaylistItem(playlist = playlist, coverId = coverId, position = nextPosition))
        return buildItemResponse(item)
    }

    @Transactional
    fun removeItem(playlistId: Long, userId: Long, itemId: Long) {
        playlistRepository.findByIdAndUserId(playlistId, userId)
            ?: throw NoSuchElementException("Playlist not found or access denied")

        if (!playlistItemRepository.existsByIdAndPlaylistId(itemId, playlistId)) {
            throw NoSuchElementException("Item not found in playlist")
        }

        playlistItemRepository.deleteById(itemId)

        // 삭제 후 position 재정렬
        val remaining = playlistItemRepository.findAllByPlaylistIdOrderByPosition(playlistId)
        remaining.forEachIndexed { index, it -> it.position = index }
    }

    @Transactional
    fun reorderItems(playlistId: Long, userId: Long, orderedItemIds: List<Long>) {
        playlistRepository.findByIdAndUserId(playlistId, userId)
            ?: throw NoSuchElementException("Playlist not found or access denied")

        val items = playlistItemRepository.findAllByPlaylistIdOrderByPosition(playlistId)
            .associateBy { it.id!! }

        orderedItemIds.forEachIndexed { index, itemId ->
            val item = items[itemId] ?: throw NoSuchElementException("Item $itemId not found in playlist")
            item.position = index
        }
    }

    private fun buildItemResponse(item: PlaylistItem): PlaylistItemResponse {
        val cover = coverRepository.findByIdOrNull(item.coverId)
        var originalTitle: String? = null
        var originalArtist: String? = null
        var originalCoverImageUrl: String? = null

        if (cover != null) {
            try {
                val music = musicClient.getMusic(cover.musicId)
                originalTitle = music.title
                originalArtist = music.artist
                originalCoverImageUrl = music.originalCoverImageUrl
            } catch (_: Exception) {}
        }

        val tags = cover?.id?.let { coverId ->
            coverTagRepository.findAllByCoverId(coverId).map { it.tag.name }
        } ?: emptyList()

        return PlaylistItemResponse(
            itemId = item.id!!,
            coverId = item.coverId,
            position = item.position,
            coverTitle = cover?.coverTitle,
            coverArtist = cover?.coverArtist,
            coverGenre = cover?.coverGenre,
            tags = tags,
            link = cover?.link,
            originalTitle = originalTitle,
            originalArtist = originalArtist,
            originalCoverImageUrl = originalCoverImageUrl,
            likeCount = cover?.likeCount ?: 0,
            viewCount = cover?.viewCount ?: 0,
        )
    }

    private fun getThumbnailUrl(musicId: Long): String? {
        return try {
            musicClient.getMusic(musicId).originalCoverImageUrl
        } catch (_: Exception) {
            null
        }
    }
}
