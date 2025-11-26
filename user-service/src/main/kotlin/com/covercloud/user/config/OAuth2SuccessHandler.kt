package com.covercloud.user.config

import com.covercloud.user.domain.Provider
import com.covercloud.user.domain.User
import com.covercloud.user.infrastructure.UserRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class OAuth2SuccessHandler(
    private val jwtProvider: JwtProvider,
    private val userRepository: UserRepository
) : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oauth2User = authentication.principal as OAuth2User
        val attributes = oauth2User.attributes

        val provider = if (attributes["kakao_account"] != null) "kakao" else "naver"

        val (socialId, nickname, profileImage, providerEnum) = when (provider) {
            "kakao" -> {
                val kakaoAccount = attributes["kakao_account"] as Map<*, *>
                val profile = kakaoAccount["profile"] as Map<*, *>

                val id = attributes["id"].toString()
                val nick = (profile["nickname"] ?: "User").toString()
                val image = (profile["profile_image_url"] ?: "").toString()

                Quadruple(id, nick, image, Provider.KAKAO)
            }

            "naver" -> {
                val responseAttr = attributes["response"] as Map<*, *>

                val id = responseAttr["id"].toString()
                val nick = (responseAttr["nickname"] ?: "User").toString()
                val image = (responseAttr["profile_image"] ?: "").toString()

                Quadruple(id, nick, image, Provider.NAVER)
            }

            else -> throw IllegalArgumentException("Unsupported provider: $provider")
        }

        val user: User = userRepository.findBySocialId(socialId)
            ?: userRepository.save(
                User(
                    socialId = socialId,
                    provider = providerEnum,
                    nickname = nickname,
                    profileImage = profileImage
                )
            )

        // 4. JWT 생성
        val token = jwtProvider.generateToken(user.id!!)

        response.sendRedirect("http://localhost:3000/success?token=$token")
    }

    data class Quadruple<A, B, C, D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
    )
}