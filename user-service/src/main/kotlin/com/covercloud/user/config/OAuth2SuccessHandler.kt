package com.covercloud.user.config

import com.covercloud.user.service.AuthService
import com.covercloud.user.domain.Provider
import com.covercloud.user.domain.User
import com.covercloud.user.repository.UserRepository
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

        val tokens = authService.generateTokens(user.id!!)

        // 개발 환경(HTTP)인지 프로덕션(HTTPS)인지 판단하여 Secure/SameSite 설정을 적절히 조정
        val isSecureRequest = request.isSecure || (request.serverName == "localhost" && request.scheme == "https")
        // request.isSecure는 HTTPS 환경에서 true입니다. 로컬에서 http로 동작할 경우 이를 false로 둡니다.

        val sameSiteValue = if (isSecureRequest) "None" else "Lax"
        val secureFlag = isSecureRequest

        logger.info("[OAuth2SuccessHandler] cookie settings -> secure=$secureFlag, sameSite=$sameSiteValue, domain='$cookieDomain'")

        // accessToken 쿠키 (Gateway가 읽어서 Authorization 헤더로 변환)
        val accessCookie = org.springframework.http.ResponseCookie.from("accessToken", tokens.accessToken)
            .path("/")
            .httpOnly(true)
            .secure(secureFlag)
            .sameSite(sameSiteValue)
            .maxAge(60L * 60L) // 1시간
            .build()
        response.addHeader("Set-Cookie", accessCookie.toString())

        // refreshToken은 nullable일 수 있으므로 존재할 때만 쿠키 설정
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

        // 인증 성공 후: 보안상 accessToken을 URL에 담지 않습니다.
        // 대신 refreshToken은 HttpOnly 쿠키로 설정되어 있으므로
        // 프론트엔드의 콜백 페이지(/auth/callback)로 리다이렉트한 뒤
        // 프론트엔드에서 POST /api/auth/refresh 를 호출하여 JSON으로 accessToken을 받아가도록 합니다.

        val redirectUrl = try {
            if (request.serverName == "localhost") {
                "http://localhost:3000/auth/callback"
            } else {
                // frontendRedirectBase 값에서 origin 부분만 추출해서 /auth/callback 으로 리다이렉트
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

    data class Quintuple<A, B, C, D, E>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D,
        val fifth: E
    )
}
