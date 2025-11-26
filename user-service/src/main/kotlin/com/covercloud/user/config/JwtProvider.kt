package com.covercloud.user.config

import com.covercloud.user.domain.User
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import jakarta.xml.bind.annotation.XmlID
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Value
import java.util.Date


@Component
class JwtProvider {

    private val secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256)

    fun generateToken(userId: Long): String {
        val now = Date()
        val exp = Date(now.time + 1000)

        return Jwts.builder()
            .setSubject(userId.toString())
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
}