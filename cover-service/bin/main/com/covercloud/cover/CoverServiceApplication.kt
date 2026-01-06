package com.covercloud.cover

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.scheduling.annotation.EnableScheduling

@EnableFeignClients
@EnableScheduling
@SpringBootApplication(scanBasePackages = ["com.covercloud.cover", "com.covercloud.shared"])
@EntityScan("com.covercloud.cover")
class CoverServiceApplication

fun main(args: Array<String>) {
    runApplication<CoverServiceApplication>(*args)
}

