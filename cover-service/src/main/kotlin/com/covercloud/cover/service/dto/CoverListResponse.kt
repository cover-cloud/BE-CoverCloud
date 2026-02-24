package com.covercloud.cover.service.dto

import com.covercloud.cover.domain.CoverGenre
import com.covercloud.cover.domain.ReportReason

data class CoverListResponse(
    val coverId: Long,
    val musicId: Long?,
    val userId: Long,
    val nickname: String,  // 작성자 닉네임 (삭제된 사용자는 "익명 사용자")
    val profileImage: String?,
    val coverArtist: String?,
    val coverTitle: String?,
    val originalArtist: String?,
    val originalTitle: String?,
    val originalCoverImageUrl: String?,
    val coverGenre: CoverGenre?,
    val link: String?,
    val viewCount: Long,
    val likeCount: Long,
    val commentCount: Long,
    val tags: List<String>,
    val createdAt: String,
    val isLiked: Boolean = false,
    val isAuthorDeleted: Boolean = false,  // 작성자가 삭제된 계정인지 표시
    val isReported: Boolean = false,  // 신고 여부
    val reportReason: ReportReason? = null,  // 신고 사유
    val reportDescription: String? = null  // 신고 설명
)
