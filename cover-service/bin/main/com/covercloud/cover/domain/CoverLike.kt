package com.covercloud.cover.domain

import com.covercloud.shared.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(name = "cover_like",
       uniqueConstraints = [UniqueConstraint(columnNames = ["cover_id", "user_id"])])
class CoverLike(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cover_id", nullable = false)
    val cover: Cover,

    @Column(name = "user_id", nullable = false)
    val userId: Long

): BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}