package com.covercloud.cover.domain

import com.covercloud.shared.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Cover (
    val link: String,
    val userId: Long,
    val musicId: Long,
    val coverArtist: String,
    var viewCount: Long = 0,
    var likeCount: Long = 0,
    var commentCount: Long = 0
): BaseEntity() {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}