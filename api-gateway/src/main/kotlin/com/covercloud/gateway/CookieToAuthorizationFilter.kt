package com.covercloud.gateway

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class CookieToAuthorizationFilter : GlobalFilter, Ordered {

    private val logger = LoggerFactory.getLogger(CookieToAuthorizationFilter::class.java)

    override fun filter(exchange: ServerWebExchange, chain: org.springframework.cloud.gateway.filter.GatewayFilterChain): Mono<Void> {
        val request: ServerHttpRequest = exchange.request
        try {
            val cookieNames = request.cookies.keys.joinToString(", ")
            logger.debug("[CookieToAuthorizationFilter] Incoming cookies: $cookieNames")
        } catch (e: Exception) {
            logger.warn("[CookieToAuthorizationFilter] failed to read cookies", e)
        }

        val cookies = request.cookies["accessToken"]
        if (!cookies.isNullOrEmpty()) {
            val accessToken: String = cookies[0].value
            logger.debug("[CookieToAuthorizationFilter] Found accessToken cookie, setting Authorization header")
            val mutatedRequest = request.mutate()
                .header("Authorization", "Bearer $accessToken")
                .build()
            val mutatedExchange = exchange.mutate().request(mutatedRequest).build()
            return chain.filter(mutatedExchange)
        } else {
            logger.debug("[CookieToAuthorizationFilter] accessToken cookie not present")
        }
        return chain.filter(exchange)
    }

    override fun getOrder(): Int = 0 // 우선순위 높게
}
