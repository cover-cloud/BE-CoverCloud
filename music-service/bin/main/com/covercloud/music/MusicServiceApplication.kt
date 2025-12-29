package com.covercloud.music

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan


@SpringBootApplication
@ComponentScan(basePackages = ["com.covercloud.music", "com.covercloud.shared"])
class MusicServiceApplication

fun main(args: Array<String>) {
    runApplication<MusicServiceApplication>(*args)
}