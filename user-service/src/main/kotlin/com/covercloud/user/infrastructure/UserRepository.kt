package com.covercloud.user.infrastructure

import com.covercloud.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository :  JpaRepository<User, Long> {
}