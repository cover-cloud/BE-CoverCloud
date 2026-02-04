package com.covercloud.user.application

import com.covercloud.shared.response.ApiResponse
import com.covercloud.shared.security.AuthenticationContext
import com.covercloud.user.application.dto.UpdateProfileRequest
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userService: UserService,
    private val authenticationContext: AuthenticationContext
) {

    @PostMapping("/profile")
    fun updateProfile(
        @RequestBody request: UpdateProfileRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<Map<String, Any?>>> {
        return try {
            val accessToken = authenticationContext.getAccessToken(httpRequest)
                ?: return ResponseEntity.status(401).body(
                    ApiResponse(success = false, message = "No access token provided")
                )

            val updated = userService.updateProfile(accessToken, request)
            ResponseEntity.ok(ApiResponse(success = true, data = updated, message = "Profile updated and new tokens issued"))
        } catch (e: IllegalArgumentException) {
            // 토큰 만료, 유효하지 않음, 사용자 미존재 등
            ResponseEntity.status(401).body(ApiResponse(success = false, message = e.message ?: "Invalid or expired access token"))
        } catch (_: Exception) {
            ResponseEntity.status(500).body(ApiResponse(success = false, message = "Internal server error"))
        }
    }

    @PostMapping("/account/delete")
    fun deleteAccount(
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<String>> {
        return try {
            val accessToken = authenticationContext.getAccessToken(httpRequest)
                ?: return ResponseEntity.status(401).body(
                    ApiResponse(success = false, message = "No access token provided")
                )

            val message = userService.deleteAccount(accessToken)
            ResponseEntity.ok(ApiResponse(success = true, message = message))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(401).body(ApiResponse(success = false, message = e.message ?: "Invalid or expired access token"))
        } catch (_: Exception) {
            ResponseEntity.status(500).body(ApiResponse(success = false, message = "Internal server error"))
        }
    }

}
