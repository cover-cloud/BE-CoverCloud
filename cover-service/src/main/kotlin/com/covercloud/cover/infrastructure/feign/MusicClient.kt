package com.covercloud.cover.infrastructure.feign

import com.covercloud.cover.infrastructure.dto.CreateMusicRequest
import com.covercloud.cover.infrastructure.dto.MusicResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(name = "music-service", url = "http://localhost:8083")
interface MusicClient {
    @PostMapping("/api/music/save")
    fun saveMusic(@RequestBody request: CreateMusicRequest) : MusicResponse

    @GetMapping("/api/music/{musicId}")
    fun getMusic(@PathVariable musicId: Long) : MusicResponse

}