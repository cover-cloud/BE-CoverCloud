package com.covercloud.user.service

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class RedisRefreshTokenService(
    private val redisTemplate: RedisTemplate<String, String>
) {

    companion object {
        private const val REFRESH_TOKEN_PREFIX = "refresh_token:"
    }

    /**
     * Refresh Token을 Redis에 저장
     * @param userId 사용자 ID
     * @param token Refresh Token
     * @param expirationTimeInSeconds 토큰 만료 시간 (초)
     */
    fun saveRefreshToken(userId: Long, token: String, expirationTimeInSeconds: Long) {
        val key = REFRESH_TOKEN_PREFIX + userId
        redisTemplate.opsForValue().set(key, token, expirationTimeInSeconds, TimeUnit.SECONDS)
    }

    /**
     * Redis에서 Refresh Token 조회
     * @param userId 사용자 ID
     * @return 저장된 Refresh Token, 없으면 null
     */
    fun getRefreshToken(userId: Long): String? {
        val key = REFRESH_TOKEN_PREFIX + userId
        return redisTemplate.opsForValue().get(key)
    }

    /**
     * Refresh Token 유효성 확인
     * @param userId 사용자 ID
     * @param token Refresh Token
     * @return 유효하면 true, 아니면 false
     */
    fun isValidRefreshToken(userId: Long, token: String): Boolean {
        val key = REFRESH_TOKEN_PREFIX + userId
        val savedToken = redisTemplate.opsForValue().get(key)
        return savedToken == token
    }

    /**
     * Refresh Token 삭제 (로그아웃)
     * @param userId 사용자 ID
     */
    fun deleteRefreshToken(userId: Long) {
        val key = REFRESH_TOKEN_PREFIX + userId
        redisTemplate.delete(key)
    }

    /**
     * 사용자의 모든 토큰 삭제 (다중 로그인 시 사용)
     * @param userId 사용자 ID
     */
    fun deleteAllTokensForUser(userId: Long) {
        val key = REFRESH_TOKEN_PREFIX + userId
        redisTemplate.delete(key)
    }
}

