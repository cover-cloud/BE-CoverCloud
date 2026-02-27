package com.covercloud.user.controller

import com.covercloud.shared.response.ApiResponse
import com.covercloud.shared.security.AuthenticationContext
import com.covercloud.user.controller.dto.CreateUploadUrlRequest
import com.covercloud.user.controller.dto.ProfileImageUploadUrlResponse
import com.covercloud.user.service.AuthService
import com.covercloud.user.service.GcsSignedUrlService
import com.covercloud.user.service.UserService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user/profile")
class ProfileMediaController(
    private val userService: UserService,
    private val authenticationContext: AuthenticationContext



) {
    @PostMapping("/upload-url")
    fun createUploadUrl(
        httpRequest: HttpServletRequest,
        @RequestBody request: CreateUploadUrlRequest
    ): ResponseEntity<ApiResponse<ProfileImageUploadUrlResponse>> {
        return try {
            val userId = authenticationContext.requireUserId(httpRequest)
            val serviceResponse = userService.createProfileImageUploadUrl(
                userId = userId,
                contentType = request.contentType
            )

            val response = ProfileImageUploadUrlResponse(
                uploadUrl = serviceResponse.uploadUrl,
                objectPath = serviceResponse.objectPath
            )

            ResponseEntity.ok(
                ApiResponse(success = true, data = response)
            )
        } catch (e: Exception) {
            e.printStackTrace()

            // 에러 메시지를 실제 에러 내용(e.message)으로 잠시 바꿔서 응답 확인
            ResponseEntity.status(500).body(
                ApiResponse(success = false, message = "에러 발생: ${e.message}")
            )
        }
    }
}