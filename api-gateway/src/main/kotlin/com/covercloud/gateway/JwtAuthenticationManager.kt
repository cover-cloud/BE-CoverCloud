package com.covercloud.gateway

import com.covercloud.shared.jwt.JwtProvider
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationManager(
    private val jwtProvider: JwtProvider
) : ReactiveAuthenticationManager {
    
    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        return Mono.just(authentication)
            .map { auth ->
                val token = auth.credentials.toString()
                
                if (jwtProvider.validateToken(token)) {
                    val userId = jwtProvider.extractUserId(token)
                    UsernamePasswordAuthenticationToken(userId, token, emptyList())
                } else {
                    throw RuntimeException("Invalid JWT token")
                }
            }
    }
}
