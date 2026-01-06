package com.covercloud.music.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class SpotifyConfig {
    @Bean
    fun spotifyAccountsClient(builder: WebClient.Builder): WebClient =
        builder.baseUrl("https://accounts.spotify.com").build()

    @Bean
    fun spotifyApiClient(builder: WebClient.Builder): WebClient =
        builder.baseUrl("https://api.spotify.com/v1").build()
}