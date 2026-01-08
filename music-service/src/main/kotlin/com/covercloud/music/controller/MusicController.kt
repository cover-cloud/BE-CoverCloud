package com.covercloud.music.controller

import com.covercloud.music.controller.dto.MusicRequest
import com.covercloud.music.service.MusicService
import com.covercloud.music.service.SpotifySearchService
import com.covercloud.music.service.dto.MusicResponse
import com.covercloud.music.service.dto.SpotifySearchRequest
import com.covercloud.music.service.dto.SpotifyTrackSummary
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/music")
class MusicController(
    private val musicService: MusicService,
    private val searchService: SpotifySearchService
) {

    @PostMapping("/save")
    fun saveMusic(@RequestBody request: MusicRequest) : MusicResponse {
        val musicResponse = musicService.saveMusic(request.toDto())
        return musicResponse
    }

    @GetMapping("/{musicId}")
    fun getMusic(@PathVariable musicId: Long): MusicResponse {
        return musicService.getMusicById(musicId)
    }

    @PostMapping("/spotify/search")
    fun search(@RequestBody request: SpotifySearchRequest): List<SpotifyTrackSummary> {
        require(request.keyword.isNotBlank()) { "검색어(keyword)는 비어 있을 수 없습니다." }
        return searchService.search(
            keyword = request.keyword,
            limit = request.limit
        )
    }

}