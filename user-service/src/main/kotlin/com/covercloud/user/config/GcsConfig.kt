package com.covercloud.user.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.io.File
import java.io.FileInputStream

@Configuration
class GcsConfig {


    @Bean
    fun storage(): Storage {
        // 프로젝트 리소스 폴더에 있는 키 파일 읽기
        val resource = ClassPathResource("key.json")
        if (!resource.exists()) {
            throw RuntimeException("GCS 키 파일을 찾을 수 없습니다! resources 폴더에 있는지 확인해주세요.")
        }

        val credentials = ServiceAccountCredentials.fromStream(resource.inputStream)
        return StorageOptions.newBuilder()
            .setCredentials(credentials) // 🔥 이 부분이 서명 키를 제공합니다.
            .setProjectId("project-a2956366-85ab-43a6-887")
            .build()
            .service
    }
}