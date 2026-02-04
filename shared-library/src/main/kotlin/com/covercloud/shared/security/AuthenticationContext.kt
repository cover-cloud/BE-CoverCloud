package com.covercloud.shared.security

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component

@Component
class AuthenticationContext {
    
    /**
     * 현재 요청에서 인증된 사용자 ID 가져오기
     */
    fun getCurrentUserId(request: HttpServletRequest): Long? {
        return request.getAttribute("userId") as? Long
    }
    
    /**
     * 현재 요청에서 인증된 사용자 ID 가져오기 (필수)
     */
    fun requireUserId(request: HttpServletRequest): Long {
        return getCurrentUserId(request) 
            ?: throw IllegalStateException("User not authenticated")
    }

    /**
     * 요청에서 액세스 토큰 가져오기 (Authorization 헤더에서)
     */
    fun getAccessToken(request: HttpServletRequest): String? {
        val authHeader = request.getHeader("Authorization")
        if (!authHeader.isNullOrBlank() && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7)
        }
        return null
    }
}
