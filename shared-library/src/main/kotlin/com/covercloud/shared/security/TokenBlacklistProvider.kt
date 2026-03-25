package com.covercloud.shared.security

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class TokenBlacklistProvider(
    private val redisTemplate: RedisTemplate<String, String>
) {

    companion object {
        private const val BLACKLIST_PREFIX = "token_blacklist:"
    }

    /**
     * Access Token을 blacklist에 추가
     * @param token Access Token
     * @param expirationTimeInSeconds 토큰 만료 시간 (초)
     */
    fun addToBlacklist(token: String, expirationTimeInSeconds: Long) {
        val key = BLACKLIST_PREFIX + token
        redisTemplate.opsForValue().set(key, "true", expirationTimeInSeconds, TimeUnit.SECONDS)
    }

    /**
     * 토큰이 blacklist에 있는지 확인
     * @param token Access Token
     * @return blacklist에 있으면 true, 없으면 false
     */
    fun isBlacklisted(token: String): Boolean {
        val key = BLACKLIST_PREFIX + token
        return redisTemplate.hasKey(key)
    }

    /**
     * Blacklist에서 토큰 제거
     * @param token Access Token
     */
    fun removeFromBlacklist(token: String) {
        val key = BLACKLIST_PREFIX + token
        redisTemplate.delete(key)
    }
}

