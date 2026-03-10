package com.covercloud.music.service

import com.covercloud.music.service.dto.ItunesTrackSummary
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class ItunesSearchService(
    @Qualifier("itunesApiClient")
    private val itunesApiClient: WebClient,
    private val objectMapper: ObjectMapper
) {

    fun search(keyword: String, limit: Int): List<ItunesTrackSummary> {
        val q = buildItunesQuery(keyword)

        val json = itunesApiClient.get()
            .uri {
                it.path("/search")
                    .queryParam("term", q)
                    .queryParam("entity", "song")
                    .queryParam("limit", limit.coerceIn(1, 200))
                    .queryParam("country", "KR")
                    .build()
            }
            .retrieve()
            .bodyToMono(String::class.java)
            .block() ?: "{}"

        val items = objectMapper.readTree(json)
            .path("results")

        if (!items.isArray) return emptyList()

        return items.map {
            ItunesTrackSummary(
                itunesTrackId = it.path("trackId").asText(),
                title = it.path("trackName").asText(),
                artist = it.path("artistName").asText("") ?: "",
                album = it.path("collectionName").asText(null),
                coverUrl = it.path("artworkUrl100").asText(null),
                previewUrl = it.path("previewUrl").asText(null),
                durationMs = it.path("trackTimeMillis").takeIf { n -> n.isNumber }?.asLong()
            )
        }
    }

    private fun buildItunesQuery(keyword: String): String {
        val k = keyword.trim()
        return when {
            k.contains(":") -> k
            k.contains(" ") -> k
            else -> k
        }
    }
}
