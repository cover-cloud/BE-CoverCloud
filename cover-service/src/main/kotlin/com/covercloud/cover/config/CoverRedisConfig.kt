package com.covercloud.cover.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.script.RedisScript

/**
 * Cover Service Redis 설정
 * 기본 RedisTemplate은 shared-library의 RedisConfig에서 제공
 * 여기서는 Like/Unlike 스크립트만 정의
 */
@Configuration
class CoverRedisConfig {

    @Bean
    fun likeScript(): RedisScript<Long> {
        val script = """
            local setKey = KEYS[1]
            local countKey = KEYS[2]
            local userId = ARGV[1]
            
            local added = redis.call('SADD', setKey, userId)
            if added == 1 then
                local newCount = redis.call('INCR', countKey)
                return newCount
            else
                return 0
            end
        """.trimIndent()
        return RedisScript.of(script, Long::class.java)
    }

    @Bean
    fun unlikeScript(): RedisScript<Long> {
        val script = """
            local setKey = KEYS[1]
            local countKey = KEYS[2]
            local userId = ARGV[1]
            
            local removed = redis.call('SREM', setKey, userId)
            if removed == 1 then
                local newCount = redis.call('DECR', countKey)
                return newCount
            else
                return -1
            end
        """.trimIndent()
        return RedisScript.of(script, Long::class.java)
    }
}
