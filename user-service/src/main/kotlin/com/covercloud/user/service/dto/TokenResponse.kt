package com.covercloud.user.service.dto

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String? = null,  // refresh 시에는 null
    val tokenType: String = "Bearer"
)

// Deprecated: RefreshTokenRequest는 쿠키로 대체됨
// data class RefreshTokenRequest(
//     val refreshToken: String
// )

data class UserInfoResponse(
    val userId: Long,
    val nickname: String,
    val profileImage: String?,
    val provider: String,
    val email: String?
)
