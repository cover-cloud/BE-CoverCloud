package com.covercloud.music.controller

import com.covercloud.music.controller.dto.MusicRequest
import com.covercloud.music.service.MusicService
import com.covercloud.music.service.dto.MusicResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/music")
class MusicController(
    private val musicService: MusicService
) {

    @PostMapping("/save")
    fun saveMusic(@RequestBody request: MusicRequest) : MusicResponse {
        val musicResponse = musicService.saveMusic(request.toDto())
        return musicResponse
    }
}