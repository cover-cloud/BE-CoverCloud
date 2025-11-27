package com.covercloud.cover.application

import com.covercloud.cover.application.dto.CoverResponse
import com.covercloud.cover.application.dto.CreateCoverRequest
import com.covercloud.cover.infrastructure.CoverRepository
import com.covercloud.cover.infrastructure.dto.CreateMusicRequest
import com.covercloud.cover.infrastructure.feign.MusicClient
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class CoverService(
    private val coverRepository: CoverRepository,
    private val musicClient: MusicClient
) {
    @Transactional
    fun uploadCover(request: CreateCoverRequest):CoverResponse {
        val musicResult = musicClient.saveMusic(
            CreateMusicRequest(
                title = request.title,
                artist = request.originalArtist,
                genre = request.genre
            )
        )

        val cover =
    }

}