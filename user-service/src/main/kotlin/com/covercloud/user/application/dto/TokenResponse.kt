package com.covercloud.user.application.dto

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String? = null,  // refresh 시에는 null
    val tokenType: String = "Bearer"
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class UserInfoResponse(
    val userId: Long,
    val nickname: String,
    val profileImage: String?,
    val provider: String,
    val email: String?
)
