package com.covercloud.music

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class MusicServiceApplication

fun main(args: Array<String>) {
    runApplication<MusicServiceApplication>(*args)
}