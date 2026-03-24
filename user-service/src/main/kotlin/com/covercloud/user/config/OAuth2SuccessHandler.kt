package com.covercloud.user.config

import com.covercloud.user.service.AuthService
import com.covercloud.user.domain.User
import com.covercloud.user.repository.UserRepository
import com.covercloud.user.config.oauth2.OAuth2UserExtractorContext
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class OAuth2SuccessHandler(
    private val authService: AuthService,
    private val userRepository: UserRepository,
    private val oauth2UserExtractorContext: OAuth2UserExtractorContext,
    @Value("\${cookie.domain:}")
    private val cookieDomain: String,
    @Value("\${frontend.redirect.base-url:https://covercloud.netlify.app/main}")
    private val frontendRedirectBase: String
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

        val userInfo = oauth2UserExtractorContext.extract(attributes)
        logger.info("[${userInfo.provider}] 추출된 사용자 정보: socialId=${userInfo.socialId}, nickname=${userInfo.nickname}")

        val user: User = userRepository.findBySocialId(userInfo.socialId)
            ?: run {
                logger.info("[${userInfo.provider}] 새 유저 생성: socialId=${userInfo.socialId}, email=${userInfo.email}")
                userRepository.save(
                    User(
                        socialId = userInfo.socialId,
                        provider = userInfo.provider,
                        nickname = userInfo.nickname,
                        profileImage = userInfo.profileImage,
                        email = userInfo.email
                    )
                )
            }

        logger.info("[${userInfo.provider}] 최종 User 정보: id=${user.id}, email=${user.email}")

        val tokens = authService.generateTokens(user.id!!)

        val isSecureRequest = request.isSecure || (request.serverName == "localhost" && request.scheme == "https")

        val sameSiteValue = if (isSecureRequest) "None" else "Lax"
        val secureFlag = isSecureRequest

        logger.info("[OAuth2SuccessHandler] cookie settings -> secure=$secureFlag, sameSite=$sameSiteValue, domain='$cookieDomain'")

        val accessCookie = org.springframework.http.ResponseCookie.from("accessToken", tokens.accessToken)
            .path("/")
            .httpOnly(true)
            .secure(secureFlag)
            .sameSite(sameSiteValue)
            .maxAge(60L * 60L) // 1시간
            .build()
        response.addHeader("Set-Cookie", accessCookie.toString())

        tokens.refreshToken?.let { rt ->
            val refreshCookieBuilder = org.springframework.http.ResponseCookie.from("refreshToken", rt)
                .path("/")
                .httpOnly(true)
                .secure(secureFlag)
                .sameSite(sameSiteValue)
                .maxAge(60L * 60L * 24L * 7L) // 7일

            if (cookieDomain.isNotBlank()) {
                refreshCookieBuilder.domain(cookieDomain)
            }

            val refreshCookie = refreshCookieBuilder.build()
            response.addHeader("Set-Cookie", refreshCookie.toString())
        }


        val redirectUrl = try {
            if (request.serverName == "localhost") {
                "http://localhost:3000/auth/callback"
            } else {
                val uri = java.net.URI.create(frontendRedirectBase)
                val portPart = if (uri.port == -1) "" else ":${uri.port}"
                "${uri.scheme}://${uri.host}${portPart}/auth/callback"
            }
        } catch (e: Exception) {
            logger.warn("[OAuth2SuccessHandler] frontendRedirectBase parse failed, fallback to /auth/callback", e)
            "/auth/callback"
        }

        response.sendRedirect(redirectUrl)
    }
}
