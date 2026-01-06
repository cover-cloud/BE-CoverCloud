package com.covercloud.cover.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {

    @Bean
    fun stringRedisTemplate(connectionFactory: LettuceConnectionFactory): StringRedisTemplate {
        val template = StringRedisTemplate()
        template.connectionFactory = connectionFactory
        return template
    }

    @Bean
    fun redisTemplate(connectionFactory: LettuceConnectionFactory): RedisTemplate<String, String> {
        val template = RedisTemplate<String, String>()
        template.connectionFactory = connectionFactory
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = StringRedisSerializer()
        template.hashKeySerializer = StringRedisSerializer()
        template.hashValueSerializer = StringRedisSerializer()
        return template
    }

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