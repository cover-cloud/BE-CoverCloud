package com.covercloud.user.service.dto

data class UploadUrlResponse(
    val objectPath: String,
    val uploadUrl: String,
    val expiresInSeconds: Long = 600
)
