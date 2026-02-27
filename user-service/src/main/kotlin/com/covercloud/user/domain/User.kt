package com.covercloud.user.domain

import com.covercloud.shared.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "user")
class User(
    val socialId: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", length = 10)
    val provider: Provider,
    var nickname: String,
    var profileImage: String? = null,
    var email: String? = null
) : BaseEntity() {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null
    var isDeleted: Boolean = false
    var deletedAt: LocalDateTime? = null

    fun updateProfile(newNickname: String?, newProfileImage: String?) {
        newNickname?.let { this.nickname = it }
        newProfileImage?.let { this.profileImage = it }
    }

    fun delete() {
        this.isDeleted = true
        this.deletedAt = LocalDateTime.now()
    }
}