package com.covercloud.user.domain

import com.covercloud.shared.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "user")
class User(
    val socialId: String,
    @Enumerated(EnumType.STRING)
    val provider: Provider? = null,
    val nickname: String,
    val profileImage: String,
) : BaseEntity() {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null
}

enum class Provider(val value: String){
    KAKAO("kakao");

    companion object {
        fun from(value: String): Provider = Provider.values().first { it.value == value }
    }
}