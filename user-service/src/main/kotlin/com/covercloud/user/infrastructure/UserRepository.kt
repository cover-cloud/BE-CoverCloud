package com.covercloud.user.infrastructure

import com.covercloud.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository :  JpaRepository<User, Long> {
    fun findBySocialId(socialId: String): User?
}