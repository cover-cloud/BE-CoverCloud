package com.covercloud.user.infrastructure

import com.covercloud.user.domain.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun findByToken(token: String): RefreshToken?
    fun findByUserId(userId: Long): RefreshToken?
    fun deleteByUserId(userId: Long)
}
