package com.covercloud.user.config

import com.covercloud.user.application.AuthService
import com.covercloud.user.domain.Provider
import com.covercloud.user.domain.User
import com.covercloud.user.infrastructure.UserRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class OAuth2SuccessHandler(
    private val authService: AuthService,
    private val userRepository: UserRepository
) : AuthenticationSuccessHandler {

    private val logger = LoggerFactory.getLogger(OAuth2SuccessHandler::class.java)

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oauth2User = authentication.principal as OAuth2User
        val attributes = oauth2User.attributes

        logger.info("[OAuth2SuccessHandler] 전체 attributes: $attributes")

        val provider = if (attributes["kakao_account"] != null) "kakao" else "naver"

        val (socialId, nickname, profileImage, email, providerEnum) = when (provider) {
            "kakao" -> {
                val kakaoAccount = attributes["kakao_account"] as Map<*, *>
                val profile = kakaoAccount["profile"] as Map<*, *>

                val id = attributes["id"].toString()
                val nick = (profile["nickname"] ?: "User").toString()
                val image = (profile["profile_image_url"] ?: "").toString()

                logger.info("[KAKAO] id=$id, nickname=$nick (이메일은 가져오지 않음)")
                Quintuple(id, nick, image, null, Provider.KAKAO)
            }

            "naver" -> {
                val responseAttr = attributes["response"] as Map<*, *>
                logger.info("[NAVER] response 내용: $responseAttr")

                val id = responseAttr["id"].toString()
                val nick = (responseAttr["nickname"] ?: "User").toString()
                val image = (responseAttr["profile_image"] ?: "").toString()
                val emailValue = responseAttr["email"]?.toString()

                logger.info("[NAVER] id=$id, nickname=$nick, email=$emailValue")
                Quintuple(id, nick, image, emailValue, Provider.NAVER)
            }

            else -> throw IllegalArgumentException("Unsupported provider: $provider")
        }

        val user: User = userRepository.findBySocialId(socialId)
            ?: run {
                logger.info("[$provider] 새 유저 생성: socialId=$socialId, email=$email")
                userRepository.save(
                    User(
                        socialId = socialId,
                        provider = providerEnum,
                        nickname = nickname,
                        profileImage = profileImage,
                        email = email
                    )
                )
            }

        logger.info("[$provider] 최종 User 정보: id=${user.id}, email=${user.email}")

        // JWT Access Token과 Refresh Token 생성
        val tokens = authService.generateTokens(user.id!!)

        // Postman 등에서 test=true 쿼리파라미터가 있으면 JSON으로 토큰 반환
        if (request.getParameter("test") == "true") {
            response.contentType = "application/json"
            response.characterEncoding = "UTF-8"
            response.writer.write("""
                {
                    "accessToken": "${tokens.accessToken}",
                    "refreshToken": "${tokens.refreshToken}",
                    "email": "$email"
                }
            """.trimIndent())
            return
        }

        // 토큰을 HttpOnly, Secure 쿠키로 설정
        val accessTokenCookie = jakarta.servlet.http.Cookie("accessToken", tokens.accessToken).apply {
            path = "/"
            isHttpOnly = true
            secure = true
            maxAge = 60 * 60 // 1시간
            domain = "localhost"
        }
        val refreshTokenCookie = jakarta.servlet.http.Cookie("refreshToken", tokens.refreshToken).apply {
            path = "/"
            isHttpOnly = true
            secure = true
            maxAge = 60 * 60 * 24 * 7 // 7일
            domain = "localhost"
        }
        response.addCookie(accessTokenCookie)
        response.addCookie(refreshTokenCookie)

        // 인증 성공 후 gateway(8080)로 리다이렉트
        response.sendRedirect("http://localhost:8080/")
    }

    data class Quintuple<A, B, C, D, E>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D,
        val fifth: E
    )
}

