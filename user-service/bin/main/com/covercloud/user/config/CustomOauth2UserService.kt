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

        val socialId: String
        val nickname: String
        val profileImage: String?
        // 네이버만 채울 예정
        val email: String? = null

        when (provider) {
            "kakao" -> {
                val kakaoAccount = attributes["kakao_account"] as Map<*, *>
                val profile = kakaoAccount["profile"] as Map<*, *>

                socialId = attributes["id"].toString()
                nickname = profile["nickname"]?.toString() ?: "Unknown"
                profileImage = profile["profile_image_url"]?.toString()
                // 카카오는 이메일 처리하지 않음 (요청: 카카오는 추가 못함)
            }

            "naver" -> {
                val response = attributes["response"] as Map<*, *>
                socialId = response["id"].toString()
                nickname = response["nickname"]?.toString() ?: "Unknown"
                profileImage = response["profile_image"]?.toString()
                // 네이버에서 email 추출
                val navEmail = response["email"]?.toString()
                // email 변수를 네이버 전용으로 사용하기 위해 재할당
                @Suppress("USELESS_CAST")
                (javaClass.getDeclaredField("email").also { it.isAccessible = true }).set(this, navEmail)
            }

            else -> throw IllegalArgumentException("Unsupported provider: $provider")
        }

        val providerEnum = when (provider.lowercase()) {
            "kakao" -> Provider.KAKAO
            "naver" -> Provider.NAVER
            else -> throw IllegalArgumentException("Unknown provider: $provider")
        }

        // User 저장 시 네이버면 email을 넣고, 아니면 빈 문자열
        val persistedUser = userRepository.findBySocialId(socialId)
            ?: userRepository.save(
                User(
                    socialId = socialId,
                    provider = providerEnum,
                    nickname = nickname,
                    profileImage = profileImage ?: "",
                    email = if (provider == "naver") {
                        // attributes에서 안전하게 꺼내기
                        val resp = attributes["response"] as? Map<*, *>
                        resp?.get("email")?.toString() ?: ""
                    } else {
                        ""
                    }
                )
            )

        val mappedAttrs: MutableMap<String, Any?> = HashMap()
        when (provider) {
            "kakao" -> {
                mappedAttrs.putAll(attributes)
                mappedAttrs["id"] = socialId
                // 카카오는 email 키 추가 안함
            }
            "naver" -> {
                val response = attributes["response"] as Map<*, *>
                response.forEach { (k, v) ->
                    mappedAttrs[k.toString()] = v
                }
                // 네이버는 email을 최상위로 추가
                val respEmail = response["email"]?.toString()
                respEmail?.let { mappedAttrs["email"] = it }
            }
            else -> {
                mappedAttrs.putAll(attributes)
            }
        }

        val userNameAttribute = if (!mappedAttrs["email"].toString().isNullOrBlank()) {
            "email"
        } else {
            userRequest.clientRegistration
                .providerDetails.userInfoEndpoint.userNameAttributeName
        }

        return DefaultOAuth2User(
            oAuth2User.authorities,
            mappedAttrs,
            userNameAttribute
        )
    }
}
