package com.covercloud.user.repository

import com.covercloud.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository :  JpaRepository<User, Long> {
    fun findBySocialId(socialId: String): User?
}