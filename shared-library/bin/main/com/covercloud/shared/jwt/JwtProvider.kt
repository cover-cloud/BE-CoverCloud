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

    // 모든 서비스에서 동일한 secret key 사용
    private val secretKey: SecretKey = run {
        val secret = "covercloud-jwt-secret-key-for-microservices-authentication-2024"
        val keyBytes = Base64.getEncoder().encode(secret.toByteArray())
        SecretKeySpec(keyBytes, 0, keyBytes.size, "HmacSHA256")
    }
    
    private val validityInMs = 1000 * 60 * 60 * 24 // 1일

    // ============================
    // 토큰 생성
    // ============================
    fun createToken(userId: Long): String {
        val now = Date()
        val expiry = Date(now.time + validityInMs)

        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("userId", userId)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(secretKey)
            .compact()
    }

    // ============================
    // 토큰 검증
    // ============================
    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    // ============================
    // 토큰에서 userId 추출
    // ============================
    fun extractUserId(token: String): Long {
        val claims = Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body

        return claims["userId"].toString().toLong()
    }

    // ============================
    // 편의용 getToken() (예: 현재 로그인 유저)
    // ============================
    fun getToken(): String {
        val fakeUserId = 1L // 테스트용 임시 사용자 ID
        return createToken(fakeUserId)

    }
}