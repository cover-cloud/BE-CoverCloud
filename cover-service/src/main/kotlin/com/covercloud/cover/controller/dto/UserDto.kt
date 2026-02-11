package com.covercloud.cover.controller.dto

import com.covercloud.cover.infrastructure.dto.UserProfileDto

data class UserDto(
    val userId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val email: String,
    val isDeleted: Boolean = false
)
// UserProfileDto -> UserDto 변환 함수
fun UserProfileDto.toUserDto(): UserDto {
    return UserDto(
        userId = this.userId,
        nickname = this.nickname,
        profileImageUrl = this.profileImageUrl,
        email = "", // UserProfileDto에 없는 필드는 기본값 처리
        isDeleted = this.isDeleted
    )
}
