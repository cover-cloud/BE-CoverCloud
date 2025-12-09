package com.covercloud.gateway

import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class AddUserIdHeaderFilter : GatewayFilter {
    
    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        return ReactiveSecurityContextHolder.getContext()
            .map { it.authentication }
            .flatMap { authentication ->
                if (authentication != null && authentication.isAuthenticated) {
                    val userId = authentication.principal.toString()
                    val modifiedRequest = exchange.request.mutate()
                        .header("X-User-Id", userId)
                        .build()
                    chain.filter(exchange.mutate().request(modifiedRequest).build())
                } else {
                    chain.filter(exchange)
                }
            }
            .switchIfEmpty(chain.filter(exchange))
    }
}
