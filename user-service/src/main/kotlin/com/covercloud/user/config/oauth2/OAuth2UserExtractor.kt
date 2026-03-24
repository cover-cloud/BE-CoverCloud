package com.covercloud.user.config.oauth2

import com.covercloud.user.domain.Provider

data class OAuth2UserInfo(
    val socialId: String,
    val nickname: String,
    val profileImage: String,
    val email: String?,
    val provider: Provider
)

interface OAuth2UserExtractor {
    fun extract(attributes: Map<*, *>): OAuth2UserInfo
    fun supports(attributes: Map<*, *>): Boolean
}

