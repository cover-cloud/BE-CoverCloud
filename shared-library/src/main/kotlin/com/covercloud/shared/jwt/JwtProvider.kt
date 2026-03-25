package com.covercloud.shared.jwt

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

    private val secret = "covercloud-jwt-secret-key-for-microservices-authentication-2024"
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray()) // 더 안전하고 표준적인 방식

    private val accessTokenValidityMs = 1000L * 60 * 60        // 1시간
    private val refreshTokenValidityMs = 1000L * 60 * 60 * 24 * 7 // 7일
    // 모든 서비스에서 동일한 secret key 사용
//    private val secretKey: SecretKey = run {
//        val secret = "covercloud-jwt-secret-key-for-microservices-authentication-2024"
//        val keyBytes = Base64.getEncoder().encode(secret.toByteArray())
//        SecretKeySpec(keyBytes, 0, keyBytes.size, "HmacSHA256")
//    }

    private val validityInMs = 1000 * 60 * 60 * 24 // 1일

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
     * Access Token 만료 시간 (ms)
     */
    fun getAccessTokenValidityMs(): Long {
        return accessTokenValidityMs
    }

    /**
     * Refresh Token 만료 시간 (ms)
     */
    fun getRefreshTokenValidityMs(): Long {
        return refreshTokenValidityMs
    }

    /**
     * 토큰에서 userId 추출
     */
    fun extractUserId(token: String): Long {
        val claims = Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body

        return claims["userId"].toString().toLong()
    }


}