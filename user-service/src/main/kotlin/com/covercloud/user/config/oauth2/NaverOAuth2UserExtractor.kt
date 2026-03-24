package com.covercloud.user.config.oauth2

import com.covercloud.user.domain.Provider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class NaverOAuth2UserExtractor : OAuth2UserExtractor {

    private val logger = LoggerFactory.getLogger(NaverOAuth2UserExtractor::class.java)

    override fun supports(attributes: Map<*, *>): Boolean {
        return attributes["response"] != null
    }

    override fun extract(attributes: Map<*, *>): OAuth2UserInfo {
        val responseAttr = attributes["response"] as Map<*, *>
        logger.info("[NAVER] response 내용: $responseAttr")

        val id = responseAttr["id"].toString()
        val nickname = (responseAttr["nickname"] ?: "User").toString()
        val profileImage = (responseAttr["profile_image"] ?: "").toString()
        val email = responseAttr["email"]?.toString()

        logger.info("[NAVER] id=$id, nickname=$nickname, email=$email")

        return OAuth2UserInfo(
            socialId = id,
            nickname = nickname,
            profileImage = profileImage,
            email = email,
            provider = Provider.NAVER
        )
    }
}

