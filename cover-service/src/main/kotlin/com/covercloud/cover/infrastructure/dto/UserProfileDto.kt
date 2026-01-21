package com.covercloud.cover.infrastructure.dto

data class UserProfileDto(
    val userId: Long,
    val nickname: String,
    val profileImageUrl: String?
)

