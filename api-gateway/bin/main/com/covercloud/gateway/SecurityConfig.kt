package com.covercloud.gateway

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
    private val jwtAuthenticationManager: JwtAuthenticationManager,
    private val jwtServerAuthenticationConverter: JwtServerAuthenticationConverter
) {
    
    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        val authenticationWebFilter = AuthenticationWebFilter(jwtAuthenticationManager).apply {
            setServerAuthenticationConverter(jwtServerAuthenticationConverter)
            // 인증 불필요한 경로는 필터 제외
            setRequiresAuthenticationMatcher(
                NegatedServerWebExchangeMatcher(
                    OrServerWebExchangeMatcher(
                        ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, "/api/cover/list"),
                        ServerWebExchangeMatchers.pathMatchers("/api/auth/**"),
                        ServerWebExchangeMatchers.pathMatchers("/oauth2/**"),
                        ServerWebExchangeMatchers.pathMatchers("/login/**"),
                        ServerWebExchangeMatchers.pathMatchers("/api/test/**")
                    )
                )
            )
        }
        
        return http
            .csrf { it.disable() }
            .cors { } // CORS 활성화
            .authorizeExchange { exchanges ->
                exchanges
                    // CORS preflight 요청 허용
                    .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    // 인증 불필요한 경로들
                    .pathMatchers(HttpMethod.GET, "/api/cover/list").permitAll()
                    .pathMatchers("/api/auth/**").permitAll()
                    .pathMatchers("/oauth2/**").permitAll()
                    .pathMatchers("/login/**").permitAll()
                    .pathMatchers("/api/test/**").permitAll()
                    // 나머지는 인증 필요
                    .anyExchange().authenticated()
            }
            .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .build()
    }
}
