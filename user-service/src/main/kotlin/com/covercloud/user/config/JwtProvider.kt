package com.covercloud.user.config

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.Base64
import java.util.Date
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec


@Component
class JwtProvider {

    // 모든 서비스에서 동일한 secret key 사용
    private val secretKey: SecretKey = run {
        val secret = "covercloud-jwt-secret-key-for-microservices-authentication-2024"
        val keyBytes = Base64.getEncoder().encode(secret.toByteArray())
        SecretKeySpec(keyBytes, 0, keyBytes.size, "HmacSHA256")
    }
    
    // Access Token: 1시간
    private val accessTokenValidityMs = 1000L * 60 * 60
    
    // Refresh Token: 7일
    private val refreshTokenValidityMs = 1000L * 60 * 60 * 24 * 7

    /**
     * Access Token 생성
     */
    fun generateAccessToken(userId: Long): String {
        val now = Date()
        val exp = Date(now.time + accessTokenValidityMs)

        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("userId", userId)
            .claim("type", "access")
            .setIssuedAt(now)
            .setExpiration(exp)
            .signWith(secretKey)
            .compact()
    }

    /**
     * Refresh Token 생성
     */
    fun generateRefreshToken(userId: Long): String {
        val now = Date()
        val exp = Date(now.time + refreshTokenValidityMs)

        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("userId", userId)
            .claim("type", "refresh")
            .setIssuedAt(now)
            .setExpiration(exp)
            .signWith(secretKey)
            .compact()
    }

    /**
     * 기존 메서드 호환성 유지 (Access Token 생성)
     */
    fun generateToken(userId: Long): String {
        return generateAccessToken(userId)
    }

    /**
     * 토큰 검증
     */
    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getUserIdFromToken(token: String): Long {
        val claims = Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body
        return claims.subject.toLong()
    }

    /**
     * 토큰 타입 확인 (access/refresh)
     */
    fun getTokenType(token: String): String {
        val claims = Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body
        return claims["type"]?.toString() ?: "access"
    }

    /**
     * Refresh Token 만료 시간 (ms)
     */
    fun getRefreshTokenValidityMs(): Long {
        return refreshTokenValidityMs
    }
}