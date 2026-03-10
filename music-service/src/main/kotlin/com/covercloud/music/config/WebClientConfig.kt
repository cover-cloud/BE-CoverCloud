package com.covercloud.music.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {

    @Bean("itunesApiClient")
    fun itunesApiClient(builder: WebClient.Builder): WebClient {
        return builder
            .baseUrl("https://itunes.apple.com")
            .build()
    }
}
