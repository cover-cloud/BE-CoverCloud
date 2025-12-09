package com.covercloud.shared.security

import com.covercloud.shared.jwt.JwtProvider
import jakarta.servlet.FilterChain
import org.springframework.stereotype.Component
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter


@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            // Gateway에서 전달한 X-User-Id 헤더 우선 확인
            val userIdFromGateway = request.getHeader("X-User-Id")
            
            if (userIdFromGateway != null) {
                logger.info("UserId from Gateway: $userIdFromGateway")
                request.setAttribute("userId", userIdFromGateway.toLong())
            } else {
                // Gateway를 거치지 않은 직접 요청인 경우 JWT 검증
                val token = resolveToken(request)
                
                if (token != null) {
                    logger.info("JWT Token found (direct request): ${token.substring(0, 20)}...")
                    
                    if (jwtProvider.validateToken(token)) {
                        val userId = jwtProvider.extractUserId(token)
                        logger.info("JWT Token validated successfully. UserId: $userId")
                        request.setAttribute("userId", userId)
                    } else {
                        logger.warn("JWT Token validation failed")
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("JWT Authentication failed: ${e.message}", e)
        }
        
        filterChain.doFilter(request, response)
    }
    
    private fun resolveToken(request: HttpServletRequest): String? {
        val header = request.getHeader("Authorization") ?: return null
        return if (header.startsWith("Bearer ")) header.substring(7) else null
    }
}