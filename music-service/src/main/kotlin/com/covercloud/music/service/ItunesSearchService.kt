package com.covercloud.music.service

import com.covercloud.music.service.dto.ItunesTrackSummary
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class ItunesSearchService(
    @Qualifier("itunesApiClient")
    private val itunesApiClient: WebClient,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun search(keyword: String, limit: Int): List<ItunesTrackSummary> {
        val q = buildItunesQuery(keyword)

        val json = itunesApiClient.get()
            .uri {
                it.path("/search")
                    .queryParam("term", q)
                    .queryParam("media", "music")
                    .queryParam("entity", "song")
                    .queryParam("limit", limit.coerceIn(1, 200))
                    // 한국 iTunes Store에는 음원 카탈로그가 없어 country=KR이면
                    // 검색어와 무관하게 항상 0건이 반환된다. US 스토어를 쓰면
                    // 한글 검색어도 정상 매칭되지만 곡명/아티스트명은 영문으로 온다.
                    .queryParam("country", "US")
                    .build()
            }
            .retrieve()
            .onStatus({ it.isError }) { res ->
                res.bodyToMono(String::class.java).defaultIfEmpty("").map { body ->
                    log.error("iTunes search failed: status={}, body={}", res.statusCode(), body)
                    IllegalStateException("iTunes search failed: ${res.statusCode()}")
                }
            }
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

    private fun buildItunesQuery(keyword: String): String = keyword.trim()
}
