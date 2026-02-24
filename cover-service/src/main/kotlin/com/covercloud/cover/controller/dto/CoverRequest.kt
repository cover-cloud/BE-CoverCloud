package com.covercloud.cover.controller.dto

import com.covercloud.cover.domain.CoverGenre
import com.covercloud.cover.service.dto.CreateServiceCoverRequest

data class CoverRequest(
    val originalTitle: String,
    val originalArtist: String,
    val genre: String?,
    val coverArtist: String?,
    val title: String?,
    val tags: List<String>?,
    val videoUrl: String?,
    val originalCoverImageUrl: String?,
){
    fun toDto(): CreateServiceCoverRequest{
        val coverGenre = genre?.let { 
            CoverGenre.valueOf(it.uppercase().replace("-", "_"))
        }
        return CreateServiceCoverRequest(
            originalArtist = originalArtist,
            originalTitle = originalTitle,
            genre = coverGenre,
            coverArtist = coverArtist,
            title = title,
            tags = tags,
            videoUrl = videoUrl,
            originalCoverImageUrl = originalCoverImageUrl
        )
    }
}