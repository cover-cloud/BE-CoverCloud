package com.covercloud.music.domain

import com.covercloud.shared.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Music (
    val title: String,
    val artist: String,
    val genre: String,
): BaseEntity() {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}