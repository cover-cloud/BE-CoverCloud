package com.covercloud.user.service

import com.covercloud.user.service.dto.UploadUrlResponse
import com.google.auth.ServiceAccountSigner
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.HttpMethod
import com.google.cloud.storage.Storage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class GcsSignedUrlService(
    private val storage: Storage,
    @Value("\${gcs.bucket}") private val bucket: String
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun createProfileUploadUrl(userId: Long, contentType: String): UploadUrlResponse {
        val ext = when (contentType.lowercase()) {
            "image/jpeg", "image/jpg" -> "jpg"
            "image/png" -> "png"
            "image/gif" -> "gif"
            else -> throw IllegalArgumentException("Unsupported content type: $contentType")
        }

        val uuid = java.util.UUID.randomUUID().toString().substring(0, 8)
        val objectPath = "users/$userId/profile_$uuid.$ext"
        val serviceAccountEmail = "covercloudofficial@gmail.com"
        val blobInfo = BlobInfo.newBuilder(bucket, objectPath)
            .setContentType(contentType)
            .build()

        val signedUrl = storage.signUrl(
            blobInfo,
            10, TimeUnit.MINUTES,
            Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
            Storage.SignUrlOption.withExtHeaders(mapOf("Content-Type" to contentType)),
            Storage.SignUrlOption.withV4Signature()
        )
        println("생성된 업로드 URL: $signedUrl")

        return UploadUrlResponse(
            objectPath = objectPath,
            uploadUrl = signedUrl.toString(),
            expiresInSeconds = 600
        )
    }

}