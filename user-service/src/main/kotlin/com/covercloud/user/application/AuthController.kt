package com.covercloud.user.application

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController {

    @GetMapping("/success")
    fun loginSuccess(): String {
        return "로그인 성공!"
    }
}