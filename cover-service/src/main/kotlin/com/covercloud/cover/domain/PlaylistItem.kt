package com.covercloud.cover.domain

import com.covercloud.shared.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "playlist_item")
class PlaylistItem(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id")
    val playlist: Playlist,

    val coverId: Long,
    var position: Int,
) : BaseEntity() {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}
