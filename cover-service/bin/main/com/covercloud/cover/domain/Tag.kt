package com.covercloud.cover.domain

import com.covercloud.shared.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Tag(
    val tag: String
): BaseEntity() {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}