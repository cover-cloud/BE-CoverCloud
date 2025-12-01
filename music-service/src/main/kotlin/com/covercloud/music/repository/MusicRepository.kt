package com.covercloud.music.repository

import com.covercloud.music.domain.Music
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MusicRepository : JpaRepository<Music, Long> {
}