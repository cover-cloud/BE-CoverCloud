package com.covercloud.cover.domain

import com.covercloud.shared.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Enumerated
import jakarta.persistence.EnumType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "cover")
class Cover (
    var link: String?,
    val userId: Long,
    var musicId: Long,
    var coverArtist: String?,
    var coverTitle: String?,
    
    @Enumerated(EnumType.STRING)
    var coverGenre: CoverGenre?,
    
    var viewCount: Long = 0,
    var likeCount: Long = 0,
    var commentCount: Long = 0
): BaseEntity() {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}