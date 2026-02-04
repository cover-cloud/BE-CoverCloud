package com.covercloud.user

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages = ["com.covercloud.user", "com.covercloud.shared"]
)
class UserServiceApplication
fun main(args: Array<String>) {
    runApplication<UserServiceApplication>(*args)
}
