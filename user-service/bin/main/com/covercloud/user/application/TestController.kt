package com.covercloud.user.application

import com.covercloud.shared.response.ApiResponse
import com.covercloud.user.application.dto.TokenResponse
import com.covercloud.user.config.JwtProvider
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/test")
class TestController(
    private val jwtProvider: JwtProvider
) {

    /**
     * 테스트용 JWT 토큰 발급
     */
    @GetMapping("/token")
    fun getTestToken(
        @RequestParam(defaultValue = "1") userId: Long
    ): ResponseEntity<ApiResponse<TokenResponse>> {
        val accessToken = jwtProvider.generateAccessToken(userId)
        val refreshToken = jwtProvider.generateRefreshToken(userId)
        
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = TokenResponse(
                    accessToken = accessToken,
                    refreshToken = refreshToken
                ),
                message = "Test tokens generated for userId: $userId"
            )
        )
    }

    /**
     * 토큰 검증 테스트
     */
    @GetMapping("/validate")
    fun validateToken(
        @RequestParam token: String
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val isValid = jwtProvider.validateToken(token)
        
        val result = if (isValid) {
            val userId = jwtProvider.getUserIdFromToken(token)
            val tokenType = jwtProvider.getTokenType(token)
            mapOf(
                "valid" to true,
                "userId" to userId,
                "tokenType" to tokenType
            )
        } else {
            mapOf("valid" to false)
        }
        
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = result
            )
        )
    }
}
