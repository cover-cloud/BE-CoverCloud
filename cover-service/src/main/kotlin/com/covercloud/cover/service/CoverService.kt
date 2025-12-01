package com.covercloud.cover.service

import com.covercloud.cover.service.dto.CoverResponse
import com.covercloud.cover.service.dto.CreateCoverRequest
import com.covercloud.cover.domain.Cover
//import com.covercloud.cover.global.security.SecurityUtil
import com.covercloud.cover.infrastructure.dto.CreateMusicRequest
import com.covercloud.cover.infrastructure.feign.MusicClient
import com.covercloud.cover.repository.CoverRepository
import com.covercloud.shared.security.JwtProvider
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class CoverService(
    private val coverRepository: CoverRepository,
    private val musicClient: MusicClient,

) {
    @Transactional
    fun uploadCover(request: CreateCoverRequest, testUserId: Long? = null): CoverResponse {
        val userId = testUserId
        val musicResult = musicClient.saveMusic(
            CreateMusicRequest(
                title = request.originalTitle,
                artist = request.originalArtist
            )
        )
        val cover = Cover(
            musicId = musicResult.id,
            userId = userId!!,
            link = request.videoUrl,
            coverArtist = request.originalArtist,
            coverGenre = request.genre,
            coverTitle = request.title
        )

        val savedCover = coverRepository.save(cover);

        return CoverResponse(
            coverId = savedCover.id!!,
            musicId = savedCover.musicId,
            coverTitle = savedCover.coverTitle,
            coverArtist = savedCover.coverArtist,
            coverGenre = savedCover.coverGenre,
            link = savedCover.link
        )
    }

}