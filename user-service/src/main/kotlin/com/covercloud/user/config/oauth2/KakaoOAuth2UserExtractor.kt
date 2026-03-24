package com.covercloud.user.config.oauth2

import com.covercloud.user.domain.Provider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class KakaoOAuth2UserExtractor : OAuth2UserExtractor {

    private val logger = LoggerFactory.getLogger(KakaoOAuth2UserExtractor::class.java)

    override fun supports(attributes: Map<*, *>): Boolean {
        return attributes["kakao_account"] != null
    }

    override fun extract(attributes: Map<*, *>): OAuth2UserInfo {
        val kakaoAccount = attributes["kakao_account"] as Map<*, *>
        val profile = kakaoAccount["profile"] as Map<*, *>

        val id = attributes["id"].toString()
        val nickname = (profile["nickname"] ?: "User").toString()
        val profileImage = (profile["profile_image_url"] ?: "").toString()

        logger.info("[KAKAO] id=$id, nickname=$nickname (이메일은 가져오지 않음)")

        return OAuth2UserInfo(
            socialId = id,
            nickname = nickname,
            profileImage = profileImage,
            email = null,
            provider = Provider.KAKAO
        )
    }
}

