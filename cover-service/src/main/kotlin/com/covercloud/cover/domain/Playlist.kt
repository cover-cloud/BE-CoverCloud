package com.covercloud.cover.domain

import com.covercloud.shared.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "playlist")
class Playlist(
    val userId: Long,
    var name: String,
) : BaseEntity() {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}
