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
    var commentCount: Long = 0,

    // 신고 관련 필드
    var isReported: Boolean = false,
    @Enumerated(EnumType.STRING)
    var reportReason: ReportReason? = null,
    var reportDescription: String? = null
): BaseEntity() {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    fun report(reason: ReportReason, description: String?) {
        this.isReported = true
        this.reportReason = reason
        this.reportDescription = description
    }
}

enum class ReportReason {
    INAPPROPRIATE_CONTENT,      // 부적절한 콘텐츠
    COPYRIGHT_INFRINGEMENT,     // 저작권 침해
    HARASSMENT,                 // 괴롭힘
    SPAM,                       // 스팸
    MISINFORMATION,             // 허위 정보
    OTHER                       // 기타
}