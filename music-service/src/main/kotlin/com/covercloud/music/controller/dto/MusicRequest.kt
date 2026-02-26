package com.covercloud.music.controller.dto

import com.covercloud.music.service.dto.CreateMusicDto
import kotlin.String

data class MusicRequest(
    val title: String,
    val artist: String,
    val originalCoverImageUrl: String

){
    fun toDto(): CreateMusicDto{
        return CreateMusicDto(
            title = title,
            artist = artist,
            originalCoverImageUrl = originalCoverImageUrl
        )
    }
}
