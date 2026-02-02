package com.covercloud.user.config

import com.covercloud.user.domain.Provider
import com.covercloud.user.domain.User
import com.covercloud.user.infrastructure.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class CustomOauth2UserService (
    private val userRepository: UserRepository
) : DefaultOAuth2UserService() {

    private val logger = LoggerFactory.getLogger(CustomOauth2UserService::class.java)

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        val provider = userRequest.clientRegistration.registrationId
        val attributes = oAuth2User.attributes

        // 전체 attributes 로깅
        logger.info("[$provider] OAuth2 전체 attributes: $attributes")

        val (socialId, nickname, profileImage, email) = when (provider) {
            "kakao" -> {
                val kakaoAccount = attributes["kakao_account"] as Map<*, *>
                val profile = kakaoAccount["profile"] as Map<*, *>

                val id = attributes["id"].toString()
                val nick = profile["nickname"]?.toString() ?: "Unknown"
                val image = profile["profile_image_url"]?.toString()

                logger.info("[KAKAO] id=$id, nickname=$nick (이메일은 가져오지 않음)")
                Quadruple(id, nick, image, null)
            }

            "naver" -> {
                val response = attributes["response"] as Map<*, *>
                logger.info("[NAVER] response 내용: $response")

                val id = response["id"].toString()
                val nick = response["nickname"]?.toString() ?: "Unknown"
                val image = response["profile_image"]?.toString()
                val emailValue = response["email"]?.toString()

                logger.info("[NAVER] id=$id, nickname=$nick, email=$emailValue")
                Quadruple(id, nick, image, emailValue)
            }

            else -> throw IllegalArgumentException("Unsupported provider: $provider")
        }

        val providerEnum = when (provider.lowercase()) {
            "kakao" -> Provider.KAKAO
            "naver" -> Provider.NAVER
            else -> throw IllegalArgumentException("Unknown provider: $provider")
        }

        val user = userRepository.findBySocialId(socialId)
            ?: run {
                logger.info("[$provider] 새 유저 생성: socialId=$socialId, email=$email")
                userRepository.save(
                    User(
                        socialId = socialId,
                        provider = providerEnum,
                        nickname = nickname,
                        profileImage = profileImage ?: "",
                        email = email
                    )
                )
            }

        // ✅ 삭제된 계정의 로그인 방지
        if (user.isDeleted) {
            logger.warn("[$provider] 삭제된 계정으로의 로그인 시도: socialId=$socialId")
            throw IllegalArgumentException("This account has been deleted and cannot be accessed")
        }

        logger.info("[$provider] 최종 User 정보: id=${user.id}, email=${user.email}")

        val userNameAttribute = userRequest.clientRegistration
            .providerDetails.userInfoEndpoint.userNameAttributeName

        return DefaultOAuth2User(
            oAuth2User.authorities,
            attributes,
            userNameAttribute
        )
    }

    data class Quadruple<A, B, C, D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
    )
}

