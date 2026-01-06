package com.covercloud.gateway

import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.http.HttpCookie
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class CookieToAuthorizationFilter : GlobalFilter, Ordered {
    override fun filter(exchange: ServerWebExchange, chain: org.springframework.cloud.gateway.filter.GatewayFilterChain): Mono<Void> {
        val request: ServerHttpRequest = exchange.request
        val cookies = request.cookies["accessToken"]
        if (!cookies.isNullOrEmpty()) {
            val accessToken: String = cookies[0].value
            val mutatedRequest = request.mutate()
                .header("Authorization", "Bearer $accessToken")
                .build()
            val mutatedExchange = exchange.mutate().request(mutatedRequest).build()
            return chain.filter(mutatedExchange)
        }
        return chain.filter(exchange)
    }

    override fun getOrder(): Int = 0 // 우선순위 높게
}
