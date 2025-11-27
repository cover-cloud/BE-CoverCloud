package com.covercloud.cover

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@EnableFeignClients
@SpringBootApplication
open class CoverServiceApplication {
    fun main(args: Array<String>) {
        runApplication<CoverServiceApplication>(*args)
    }

}