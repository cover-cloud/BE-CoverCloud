package com.covercloud.shared

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import java.time.LocalDateTime

@MappedSuperclass
open class BaseEntity {
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now();
    val updatedAt: LocalDateTime = LocalDateTime.now()

}