package com.covercloud.user.config

import com.covercloud.user.application.AuthService
import com.covercloud.user.application.dto.TokenResponse
import com.covercloud.user.domain.User
import com.covercloud.user.infrastructure.UserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class OAuth2SuccessHandlerTest {
    @Test
    fun kakaoSocialLoginRedirectAndCookie() {
        // given
        val authService = mock<AuthService>()
        val userRepository = mock<UserRepository>()
        val handler = OAuth2SuccessHandler(authService, userRepository)

        val kakaoAttributes = mapOf(
            "id" to "123456",
            "kakao_account" to mapOf(
                "profile" to mapOf(
                    "nickname" to "테스트유저",
                    "profile_image_url" to "http://test.com/image.jpg"
                ),
                "email" to "test@kakao.com"
            )
        )
        val authorities = setOf(OAuth2UserAuthority(kakaoAttributes))
        val oAuth2User = DefaultOAuth2User(authorities, kakaoAttributes, "id")

        val authentication = mock<Authentication>()
        whenever(authentication.principal).thenReturn(oAuth2User)

        val user = mock<User>()
        whenever(userRepository.findBySocialId(any())).thenReturn(user)
        whenever(authService.generateTokens(any())).thenReturn(
            com.covercloud.user.application.dto.TokenResponse(
                accessToken = "access-token-test",
                refreshToken = "refresh-token-test"
            )
        )

        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()

        // when
        handler.onAuthenticationSuccess(request, response, authentication)

        // then
        assertEquals("http://localhost:3000/auth/callback?accessToken=access-token-test", response.redirectedUrl)
        val refreshTokenCookie = response.cookies.find { it.name == "refreshToken" }
        assertEquals("refresh-token-test", refreshTokenCookie?.value)
    }

    @Test
    fun naverSocialLoginRedirectAndCookie() {
        // given
        val authService = mock<AuthService>()
        val userRepository = mock<UserRepository>()
        val handler = OAuth2SuccessHandler(authService, userRepository)

        val naverAttributes = mapOf(
            "response" to mapOf(
                "id" to "naver123",
                "nickname" to "네이버유저",
                "profile_image" to "http://test.com/naver.jpg",
                "email" to "naver@test.com"
            )
        )
        val authorities = setOf(OAuth2UserAuthority(naverAttributes))
        val oAuth2User = DefaultOAuth2User(authorities, naverAttributes, "response")

        val authentication = mock<Authentication>()
        whenever(authentication.principal).thenReturn(oAuth2User)

        val user = mock<User> {
            whenever(it.id).thenReturn(2L)
        }
        whenever(userRepository.findBySocialId("naver123")).thenReturn(user)
        whenever(authService.generateTokens(2L)).thenReturn(
            TokenResponse(
                accessToken = "naver-access-token",
                refreshToken = "naver-refresh-token"
            )
        )

        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()

        // when
        handler.onAuthenticationSuccess(request, response, authentication)

        // then
        assertEquals("http://localhost:3000/auth/callback?accessToken=naver-access-token", response.redirectedUrl)
        val refreshTokenCookie = response.cookies.find { it.name == "refreshToken" }
        assertEquals("naver-refresh-token", refreshTokenCookie?.value)
    }


}