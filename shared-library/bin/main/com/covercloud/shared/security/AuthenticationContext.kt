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
}
