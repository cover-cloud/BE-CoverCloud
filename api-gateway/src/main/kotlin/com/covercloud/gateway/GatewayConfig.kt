package com.covercloud.gateway

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Configuration
class GatewayConfig {
    
    @Bean
    fun corsWebFilter(): CorsWebFilter {
        val corsConfig = CorsConfiguration()
        // 명시적 origin 목록 대신 패턴을 사용하면 서브도메인 등 유연하게 허용할 수 있습니다.
        corsConfig.allowedOriginPatterns = listOf(
            "http://localhost:3000",
            "http://localhost:3001",
            "https://www.covercloud.kr",
            "https://covercloud-dev.netlify.app",
            "https://covercloud.kr"
        )
        corsConfig.maxAge = 3600L
        corsConfig.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
        corsConfig.allowedHeaders = listOf("*")
        // 응답 헤더 노출 (참고: 브라우저는 Set-Cookie를 JS에서 읽을 수 없지만, 쿠키 자체는 credentials가 허용되고
        // 응답에 Set-Cookie가 있을 때 브라우저가 자동으로 저장합니다.)
        corsConfig.exposedHeaders = listOf("Authorization", "Content-Type", "X-User-Id", "Set-Cookie")
        // 쿠키를 사용하려면 반드시 true
        corsConfig.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", corsConfig)

        return CorsWebFilter(source)
    }
}
