package com.covercloud.user.domain

import com.covercloud.shared.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "user")
class User(
    val socialId: String,
    val provider: String,
    val nickname: String,
    val profileImage: String,
) : BaseEntity() {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null
}