package com.covercloud.user.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig (
    private val customOAuth2UserService: CustomOauth2UserService,
    private val oAuth2SuccessHandler: OAuth2SuccessHandler
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {

        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers("/", "/auth/**", "/api/auth/**", "/api/user/**", "/api/test/**", "/login-test.html", "/static/**").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2Login {
//                it.userInfoEndpoint { userInfo ->
//                    userInfo.userService(customOAuth2UserService)
//                }
//                it.defaultSuccessUrl("/auth/success", true)
                    oauth ->
                oauth
                    .userInfoEndpoint {
                        it.userService(customOAuth2UserService)
                    }
                    .successHandler(oAuth2SuccessHandler)
            }

        return http.build()
    }
}