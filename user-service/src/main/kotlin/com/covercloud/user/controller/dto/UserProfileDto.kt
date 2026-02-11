package com.covercloud.user.controller.dto

data class UserProfileDto(
    val userId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val email: String,
    val isDeleted: Boolean = false  // 삭제된 계정 여부
)

