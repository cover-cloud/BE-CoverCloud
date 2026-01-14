package com.covercloud.user.application

import com.covercloud.shared.response.ApiResponse
import org.springframework.web.bind.annotation.CookieValue
import jakarta.servlet.http.HttpServletResponse
import com.covercloud.user.application.dto.TokenResponse
import com.covercloud.user.application.dto.UserInfoResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    /**
     * 로그인 성공 후 테스트용 엔드포인트
     */
    @GetMapping("/success")
    fun loginSuccess(): String {
        return "로그인 성공!"
    }

    /**
     * 카카오/네이버 로그인 성공 후 토큰 확인 페이지
     */
    @GetMapping("/login-success")
    fun loginSuccessPage(
        @RequestParam accessToken: String,
        @RequestParam refreshToken: String
    ): String {
        val userId = authService.getUserInfo(accessToken).userId
        val userInfo = authService.getUserInfo(accessToken)
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>로그인 성공</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        max-width: 800px;
                        margin: 50px auto;
                        padding: 20px;
                        background-color: #f5f5f5;
                    }
                    .container {
                        background: white;
                        padding: 30px;
                        border-radius: 10px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    h1 {
                        color: #4CAF50;
                        text-align: center;
                    }
                    .token-box {
                        background: #f9f9f9;
                        border: 1px solid #ddd;
                        border-radius: 5px;
                        padding: 15px;
                        margin: 20px 0;
                        word-wrap: break-word;
                    }
                    .label {
                        font-weight: bold;
                        color: #333;
                        margin-bottom: 10px;
                    }
                    .token {
                        font-family: monospace;
                        font-size: 12px;
                        color: #666;
                        background: white;
                        padding: 10px;
                        border-radius: 3px;
                        overflow-wrap: break-word;
                    }
                    .user-info {
                        background: #e3f2fd;
                        padding: 15px;
                        border-radius: 5px;
                        margin: 20px 0;
                    }
                    .success-icon {
                        font-size: 50px;
                        text-align: center;
                        margin-bottom: 20px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="success-icon">✅</div>
                    <h1>카카오 로그인 성공!</h1>
                    
                    <div class="user-info">
                        <div class="label">사용자 정보</div>
                        <p><strong>User ID:</strong> ${userInfo.userId}</p>
                        <p><strong>Nickname:</strong> ${userInfo.nickname}</p>
                        <p><strong>Provider:</strong> ${userInfo.provider}</p>
                    </div>
                    
                    <div class="token-box">
                        <div class="label">🔑 Access Token (1시간 유효)</div>
                        <div class="token">$accessToken</div>
                    </div>
                    
                    <div class="token-box">
                        <div class="label">🔄 Refresh Token (7일 유효)</div>
                        <div class="token">$refreshToken</div>
                    </div>
                    
                    <div style="text-align: center; margin-top: 30px;">
                        <p>✨ JWT 토큰이 성공적으로 발급되었습니다!</p>
                        <p style="color: #666; font-size: 14px;">위 토큰을 복사하여 API 요청 시 사용하세요.</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    /**
     * Refresh Token으로 Access Token 재발급
     * refreshToken은 HttpOnly 쿠키에서 자동으로 가져옴
     */
    @PostMapping("/refresh")
    fun refreshToken(
        @CookieValue(name = "refreshToken", required = false) refreshToken: String?,
        response: HttpServletResponse
    ): ResponseEntity<ApiResponse<TokenResponse>> {
        if (refreshToken == null) {
            return ResponseEntity.status(401).body(
                ApiResponse(
                    success = false,
                    data = null,
                    message = "Refresh token not found in cookies"
                )
            )
        }

        val tokens = authService.refreshAccessToken(refreshToken)

        // 새로운 accessToken을 응답 바디에만 담음
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = tokens,
                message = "Token refreshed successfully"
            )
        )
    }

    /**
     * 현재 사용자 정보 조회
     */
    @GetMapping("/me")
    fun getUserInfo(
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<ApiResponse<UserInfoResponse>> {
        val token = authHeader.removePrefix("Bearer ")
        val userInfo = authService.getUserInfo(token)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = userInfo
            )
        )
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    fun logout(
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<ApiResponse<String>> {
        val token = authHeader.removePrefix("Bearer ")
        val userId = authService.getUserInfo(token).userId
        authService.logout(userId)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Logged out successfully"
            )
        )
    }

    /**
     * 테스트용 토큰 생성 (개발 환경 전용)
     */
    @GetMapping("/test/token")
    fun generateTestToken(
        @RequestParam(defaultValue = "1") userId: Long
    ): ResponseEntity<ApiResponse<TokenResponse>> {
        val tokens = authService.generateTokens(userId)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = tokens,
                message = "Test token generated for userId: $userId"
            )
        )
    }
}