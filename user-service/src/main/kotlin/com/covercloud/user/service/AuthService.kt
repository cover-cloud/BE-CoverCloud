package com.covercloud.user.service

import com.covercloud.shared.jwt.JwtProvider
import com.covercloud.user.service.dto.TokenResponse
import com.covercloud.user.service.dto.UserInfoResponse
import com.covercloud.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val jwtProvider: JwtProvider,
    private val userRepository: UserRepository,
    private val redisRefreshTokenService: RedisRefreshTokenService,
    private val tokenBlacklistService: TokenBlacklistService
) {

    /**
     * Access Token과 Refresh Token 생성
     */
    fun generateTokens(userId: Long): TokenResponse {
        val accessToken = jwtProvider.generateAccessToken(userId)
        val refreshToken = jwtProvider.generateRefreshToken(userId)

        // Redis에 Refresh Token 저장
        val refreshTokenValiditySeconds = jwtProvider.getRefreshTokenValidityMs() / 1000
        redisRefreshTokenService.saveRefreshToken(userId, refreshToken, refreshTokenValiditySeconds)

        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    /**
     * Refresh Token으로 새로운 Access Token 발급 (Refresh Token은 유지)
     */
    fun refreshAccessToken(refreshToken: String): TokenResponse {
        // Refresh Token 유효성 검증
        if (!jwtProvider.validateToken(refreshToken)) {
            throw IllegalArgumentException("Invalid refresh token")
        }

        // Refresh Token 타입 확인
        if (jwtProvider.getTokenType(refreshToken) != "refresh") {
            throw IllegalArgumentException("Not a refresh token")
        }

        val userId = jwtProvider.getUserIdFromToken(refreshToken)

        // Redis에서 Refresh Token 검증
        if (!redisRefreshTokenService.isValidRefreshToken(userId, refreshToken)) {
            throw IllegalArgumentException("Refresh token not found or invalid")
        }

        // 새로운 Access Token만 생성
        val newAccessToken = jwtProvider.generateAccessToken(userId)

        return TokenResponse(
            accessToken = newAccessToken
            // refreshToken은 반환하지 않음 (클라이언트가 기존 것을 계속 사용)
        )
    }

    /**
     * 토큰으로 사용자 정보 조회
     */
    fun getUserInfo(accessToken: String): UserInfoResponse {
        if (!jwtProvider.validateToken(accessToken)) {
            throw IllegalArgumentException("Invalid access token")
        }

        val userId = jwtProvider.getUserIdFromToken(accessToken)
        val user = userRepository.findById(userId).orElseThrow {
            throw IllegalArgumentException("User not found")
        }

        // ✅ 삭제된 계정의 접근 방지
        if (user.isDeleted) {
            throw IllegalArgumentException("This account has been deleted and cannot be accessed")
        }

        return UserInfoResponse(
            userId = user.id!!,
            nickname = user.nickname,
            profileImage = user.profileImage,
            provider = user.provider.name,
            email = user.email
        )
    }

    /**
     * 로그아웃 (Refresh Token 삭제 및 Access Token blacklist 처리)
     */
    fun logout(userId: Long, accessToken: String) {
        redisRefreshTokenService.deleteRefreshToken(userId)

        val accessTokenValiditySeconds = jwtProvider.getAccessTokenValidityMs() / 1000
        tokenBlacklistService.addToBlacklist(accessToken, accessTokenValiditySeconds)
    }
}
