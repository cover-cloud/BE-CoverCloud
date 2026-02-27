package com.covercloud.music.service

import com.covercloud.music.service.dto.SpotifyTrackSummary
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.socket.client.WebSocketClient

@Service
class SpotifySearchService(
    @Qualifier("spotifyApiClient")
    private val spotifyApiClient: WebClient,
    private val spotifyTokenService: SpotifyTokenService,
    private val objectMapper: ObjectMapper
) {

    fun search(keyword: String, limit: Int): List<SpotifyTrackSummary> {
        val q = buildSpotifyQuery(keyword)

        val json = spotifyApiClient.get()
            .uri {
                it.path("/search")
                    .queryParam("type", "track")
                    .queryParam("q", q)
                    .queryParam("limit", limit.coerceIn(1, 50))
                    .queryParam("market", "KR") // ✅ 한글/국내곡 검색 안정화
                    .build()
            }
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${spotifyTokenService.getToken()}")
            .header(HttpHeaders.ACCEPT_LANGUAGE, "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
            .retrieve()
            .bodyToMono(String::class.java)
            .block() ?: "{}"

        val items = objectMapper.readTree(json)
            .path("tracks")
            .path("items")

        if (!items.isArray) return emptyList()

        return items.map {
            SpotifyTrackSummary(
                spotifyTrackId = it.path("id").asText(),
                title = it.path("name").asText(),
                artist = it.path("artists").firstOrNull()?.path("name")?.asText() ?: "",
                album = it.path("album").path("name").asText(null),
                coverUrl = it.path("album").path("images").firstOrNull()?.path("url")?.asText(null),
                previewUrl = it.path("preview_url").asText(null),
                durationMs = it.path("duration_ms").takeIf { n -> n.isNumber }?.asLong()
            )
        }
    }

    // ✅ 언어(한글/영문) 구분 없이 잘 나오게 하는 쿼리 보정
    private fun buildSpotifyQuery(keyword: String): String {
        val k = keyword.trim()
        return when {
            k.contains(":") -> k                 // 고급 검색(track:, artist:) 그대로 허용
            k.contains(" ") -> k                 // 여러 단어면 자연 검색
            else -> "track:$k"                   // 한 단어면 track 중심 검색
        }
    }
}
