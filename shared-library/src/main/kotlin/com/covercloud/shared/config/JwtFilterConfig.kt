package com.covercloud.shared.config

import com.covercloud.shared.security.JwtAuthenticationFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JwtFilterConfig {

    @Bean
    fun jwtFilter(jwtAuthenticationFilter: JwtAuthenticationFilter): FilterRegistrationBean<JwtAuthenticationFilter> {
        val registrationBean = FilterRegistrationBean<JwtAuthenticationFilter>()
        registrationBean.filter = jwtAuthenticationFilter
        registrationBean.addUrlPatterns("/api/*")
        return registrationBean
    }
}
