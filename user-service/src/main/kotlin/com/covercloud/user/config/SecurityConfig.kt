    package com.covercloud.user.config

    import org.springframework.context.annotation.Bean
    import org.springframework.context.annotation.Configuration
    import org.springframework.security.config.annotation.web.builders.HttpSecurity
    import org.springframework.security.web.SecurityFilterChain

    @Configuration
    class SecurityConfig(
        private val customOAuth2UserService: CustomOauth2UserService,
        private val oAuth2SuccessHandler: OAuth2SuccessHandler
    ) {

        @Bean
        fun filterChain(http: HttpSecurity): SecurityFilterChain {

            http
                .csrf { it.disable() }
                .httpBasic { it.disable() }
                .formLogin { it.disable() }

                .authorizeHttpRequests { auth ->
                    auth
                        // ✅ 정적 리소스 (Spring Boot 기본 위치들)
                        .requestMatchers(
                            "/css/**", "/js/**", "/images/**", "/webjars/**",
                            "/favicon.ico", "/error"
                        ).permitAll()

                        // ✅ OAuth2 엔드포인트
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

                        // ✅ 기존 permit
                        .requestMatchers(
                            "/", "/auth/**", "/api/auth/**", "/api/user/**", "/api/test/**",
                            "/login-test.html"
                        ).permitAll()

                        .anyRequest().authenticated()
                }

                .oauth2Login { oauth ->
                    oauth
                        .userInfoEndpoint { it.userService(customOAuth2UserService) }
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler { request, response, exception ->
                            response.sendRedirect("http://localhost:8080/login?error")
                        }
                }

            return http.build()
        }

    }