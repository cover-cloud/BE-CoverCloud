package com.covercloud.music.service

import com.covercloud.music.domain.Music
import com.covercloud.music.repository.MusicRepository
import com.covercloud.music.service.dto.CreateMusicDto
import com.covercloud.music.service.dto.MusicResponse
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class MusicService(
    private val musicRepository: MusicRepository

) {
    @Transactional
    fun saveMusic(request: CreateMusicDto): MusicResponse {
        val existing = musicRepository.findByTitleAndArtist(request.title, request.artist)

        if(existing != null) {
            return MusicResponse(
                id = existing.id!!,
                title = existing.title,
                artist = existing.artist,
            )
        }
        val music = Music(
            title = request.title,
            artist = request.artist
        )
        val savedMusic = musicRepository.save(music)

        return MusicResponse(
            id = savedMusic.id!!,
            title = savedMusic.title,
            artist = savedMusic.artist,
        )

    }

}