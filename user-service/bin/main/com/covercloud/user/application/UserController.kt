package com.covercloud.user.application

import com.covercloud.shared.response.ApiResponse
import com.covercloud.user.application.dto.UpdateProfileRequest
import com.covercloud.user.application.dto.UserInfoResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
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
    ): ResponseEntity<ApiResponse<UserInfoResponse>> {
        if (accessToken.isNullOrBlank()) {
            return ResponseEntity.status(401).body(ApiResponse(success = false, message = "No access token cookie"))
        }
        val updated = userService.updateProfile(accessToken, request)
        return ResponseEntity.ok(ApiResponse(success = true, data = updated, message = "Profile updated"))
    }
}
