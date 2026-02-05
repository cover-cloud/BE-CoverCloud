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
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/gif" -> "gif"
            else -> throw IllegalArgumentException("Unsupported content type: $contentType")
        }

        val objectPath = "users/$userId/profile.$ext"
        val serviceAccountEmail = "covercloudofficial@gmail.com"
        val blobInfo = BlobInfo.newBuilder(bucket, objectPath)
            .setContentType(contentType)
            .build()

        val signedUrl = storage.signUrl(
            blobInfo,
            10, TimeUnit.MINUTES,
            Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
            Storage.SignUrlOption.withContentType(),
            // V4 서명을 사용하면 로컬에서도 GCS가 신원을 확실히 인증합니다.
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