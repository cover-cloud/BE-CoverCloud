package com.covercloud.user.application

import com.covercloud.shared.jwt.JwtProvider
import com.covercloud.user.application.dto.TokenResponse
import com.covercloud.user.application.dto.UserInfoResponse
import com.covercloud.user.domain.RefreshToken
import com.covercloud.user.infrastructure.RefreshTokenRepository
import com.covercloud.user.infrastructure.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AuthService(
    private val jwtProvider: JwtProvider,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userRepository: UserRepository
) {

    /**
     * Access Token과 Refresh Token 생성
     */
    @Transactional
    fun generateTokens(userId: Long): TokenResponse {
        val accessToken = jwtProvider.generateAccessToken(userId)
        val refreshToken = jwtProvider.generateRefreshToken(userId)

        // 기존 Refresh Token 삭제
        refreshTokenRepository.findByUserId(userId)?.let {
            refreshTokenRepository.delete(it)
        }

        // 새 Refresh Token 저장
        val expiryDate = LocalDateTime.now()
            .plusSeconds(jwtProvider.getRefreshTokenValidityMs() / 1000)

        refreshTokenRepository.save(
            RefreshToken(
                userId = userId,
                token = refreshToken,
                expiryDate = expiryDate
            )
        )

        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    /**
     * Refresh Token으로 새로운 Access Token 발급 (Refresh Token은 유지)
     */
    @Transactional
    fun refreshAccessToken(refreshToken: String): TokenResponse {
        // Refresh Token 유효성 검증
        if (!jwtProvider.validateToken(refreshToken)) {
            throw IllegalArgumentException("Invalid refresh token")
        }

        // Refresh Token 타입 확인
        if (jwtProvider.getTokenType(refreshToken) != "refresh") {
            throw IllegalArgumentException("Not a refresh token")
        }

        // DB에서 Refresh Token 확인
        val savedToken = refreshTokenRepository.findByToken(refreshToken)
            ?: throw IllegalArgumentException("Refresh token not found")

        // 만료 시간 확인
        if (savedToken.expiryDate.isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(savedToken)
            throw IllegalArgumentException("Refresh token expired")
        }

        val userId = savedToken.userId

        // 새로운 Access Token만 생성 (Refresh Token은 유지)
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
     * 로그아웃 (Refresh Token 삭제)
     */
    @Transactional
    fun logout(userId: Long) {
        refreshTokenRepository.deleteByUserId(userId)
    }
}
