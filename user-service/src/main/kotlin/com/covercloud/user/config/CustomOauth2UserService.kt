package com.covercloud.user.config

import com.covercloud.user.domain.Provider
import com.covercloud.user.domain.User
import com.covercloud.user.infrastructure.UserRepository
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class CustomOauth2UserService (
    private val userRepository: UserRepository
) : DefaultOAuth2UserService() {

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        val provider = userRequest.clientRegistration.registrationId
        val attributes = oAuth2User.attributes

        val (socialId, nickname, email) = when (provider) {
            "kakao" -> {
                val kakaoAccount = attributes["kakao_account"] as Map<*, *>
                val profile = kakaoAccount["profile"] as Map<*, *>

                Triple(
                    attributes["id"].toString(),
                    profile["nickname"]?.toString() ?: "Unknown",
                    kakaoAccount["email"]?.toString()
                )
            }

            "naver" -> {
                val response = attributes["response"] as Map<*, *>
                Triple(
                    response["id"].toString(),
                    response["nickname"]?.toString() ?: "Unknown",
                    response["email"]?.toString()
                )
            }

            else -> throw IllegalArgumentException("Unsupported provider: $provider")
        }

        val providerEnum = when (provider.lowercase()) {
            "kakao" -> Provider.KAKAO
            "naver" -> Provider.NAVER
            else -> throw IllegalArgumentException("Unknown provider: $provider")
        }

        // 기존 유저 검색 또는 자동 가입
        val user = userRepository.findBySocialId(socialId)
            ?: userRepository.save(
                User(
                    socialId = socialId,
                    provider = providerEnum,
                    nickname = nickname,
                    profileImage = ""
                )
            )

        // 스프링 시큐리티 인증 객체 생성
        val userNameAttribute = userRequest.clientRegistration
            .providerDetails.userInfoEndpoint.userNameAttributeName

        return DefaultOAuth2User(
            oAuth2User.authorities,
            attributes,
            userNameAttribute
        )
    }
}
