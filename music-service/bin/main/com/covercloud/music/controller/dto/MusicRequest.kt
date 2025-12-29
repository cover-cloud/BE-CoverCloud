package com.covercloud.music.controller.dto

import com.covercloud.music.service.dto.CreateMusicDto

data class MusicRequest(
    val title: String,
    val artist: String
){
    fun toDto(): CreateMusicDto{
        return CreateMusicDto(
            title = title,
            artist = artist)
    }
}
