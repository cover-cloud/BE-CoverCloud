package com.covercloud.cover

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class CoverServiceApplication {
    fun main(args: Array<String>) {
        runApplication<CoverServiceApplication>(*args)
    }

}