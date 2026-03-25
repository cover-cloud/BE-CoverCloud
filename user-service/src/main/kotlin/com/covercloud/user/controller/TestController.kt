package com.covercloud.user.controller

import com.covercloud.shared.jwt.JwtProvider
import com.covercloud.shared.response.ApiResponse
import com.covercloud.user.service.AuthService
import com.covercloud.user.service.dto.TokenResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/test")
class TestController(
    private val jwtProvider: JwtProvider,
    private val authService: AuthService
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
     * 테스트용 로그인 (토큰 생성 및 Redis에 저장)
     */
    @PostMapping("/login")
    fun testLogin(
        @RequestParam(defaultValue = "1") userId: Long
    ): ResponseEntity<ApiResponse<TokenResponse>> {
        val tokens = authService.generateTokens(userId)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = tokens,
                message = "Test login successful for userId: $userId"
            )
        )
    }

    /**
     * 테스트용 로그아웃 (Refresh Token 삭제, Access Token blacklist 추가)
     */
    @PostMapping("/logout")
    fun testLogout(
        @RequestParam userId: Long,
        @RequestParam accessToken: String
    ): ResponseEntity<ApiResponse<String>> {
        return try {
            authService.logout(userId, accessToken)
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Test logout successful for userId: $userId"
                )
            )
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(
                ApiResponse(
                    success = false,
                    message = "Logout failed: ${e.message}"
                )
            )
        }
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

    /**
     * Refresh Token으로 새로운 Access Token 발급 테스트
     */
    @PostMapping("/refresh")
    fun testRefreshToken(
        @RequestParam refreshToken: String
    ): ResponseEntity<ApiResponse<TokenResponse>> {
        return try {
            val newTokens = authService.refreshAccessToken(refreshToken)
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    data = newTokens,
                    message = "Token refresh successful"
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(401).body(
                ApiResponse(
                    success = false,
                    message = "Token refresh failed: ${e.message}"
                )
            )
        }
    }
}
