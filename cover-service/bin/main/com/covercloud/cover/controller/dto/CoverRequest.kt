package com.covercloud.cover.controller.dto

import com.covercloud.cover.service.dto.CreateServiceCoverRequest

data class CoverRequest(
    val originalTitle: String,
    val originalArtist: String,
    val genre: String?,
    val coverArtist: String?,
    val title: String?,
    val tags: List<String>?,
    val videoUrl: String?
){
    fun toDto(): CreateServiceCoverRequest{
        return CreateServiceCoverRequest(
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