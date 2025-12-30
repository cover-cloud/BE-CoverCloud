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

@Entity
@Table(name = "comment")
class Comment(
    val content: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="cover_id", nullable = false)
    val cover: Cover,

    @Column(name="user_id" , nullable = false)
    val userId: Long,

    @Column(name="parent_comment_id")
    val parentCommentId: Long? = null

): BaseEntity() {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}