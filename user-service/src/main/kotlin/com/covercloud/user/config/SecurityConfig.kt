package com.covercloud.user.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig (
    private val customOAuth2UserService: CustomOauth2UserService
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {

        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers("/", "/auth/**").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2Login {
                it.userInfoEndpoint { userInfo ->
                    userInfo.userService(customOAuth2UserService)
                }
                it.defaultSuccessUrl("/auth/success", true)
            }

        return http.build()
    }
}