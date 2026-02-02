package com.covercloud.user.application

import com.covercloud.shared.response.ApiResponse
import com.covercloud.user.application.dto.UpdateProfileRequest
import com.covercloud.user.application.dto.UserInfoResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userService: UserService
) {

    @PostMapping("/profile")
    fun updateProfile(
        @CookieValue("accessToken") accessToken: String?,
        @RequestBody request: UpdateProfileRequest
    ): ResponseEntity<ApiResponse<Map<String, Any?>>> {
        if (accessToken.isNullOrBlank()) {
            return ResponseEntity.status(401).body(ApiResponse(success = false, message = "No access token cookie"))
        }

        return try {
            val updated = userService.updateProfile(accessToken, request)
            ResponseEntity.ok(ApiResponse(success = true, data = updated, message = "Profile updated and new tokens issued"))
        } catch (e: IllegalArgumentException) {
            // 토큰 만료, 유효하지 않음, 사용자 미존재 등
            ResponseEntity.status(401).body(ApiResponse(success = false, message = "Invalid or expired access token"))
        }
    }

    @PostMapping("/account/delete")
    fun deleteAccount(
        @CookieValue("accessToken") accessToken: String?
    ): ResponseEntity<ApiResponse<String>> {
        if (accessToken.isNullOrBlank()) {
            return ResponseEntity.status(401).body(ApiResponse(success = false, message = "No access token cookie"))
        }

        return try {
            val message = userService.deleteAccount(accessToken)
            ResponseEntity.ok(ApiResponse(success = true, message = message))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(401).body(ApiResponse(success = false, message = "Invalid or expired access token"))
        }
    }

}
