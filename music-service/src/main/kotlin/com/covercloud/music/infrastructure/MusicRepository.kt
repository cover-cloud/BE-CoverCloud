package com.covercloud.music.infrastructure

import com.covercloud.music.domain.Music
import org.springframework.data.jpa.repository.JpaRepository

interface MusicRepository : JpaRepository<Music, Long>{
}