package com.covercloud.cover.controller.dto

import com.covercloud.cover.service.dto.CreateCoverRequest

data class CoverRequest(
    val originalTitle: String?,
    val originalArtist: String?,
    val genre: String?,
    val coverArtist: String?,
    val title: String?,
    val tags: List<String>?,
    val videoUrl: String?
){
    fun toDto(): CreateCoverRequest{
        return CreateCoverRequest(
            originalArtist = originalArtist,
            originalTitle = originalTitle,
            genre = genre,
            coverArtist = coverArtist,
            title = title,
            tags = tags,
            videoUrl = videoUrl
        )
    }
}