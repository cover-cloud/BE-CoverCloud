package com.covercloud.user.controller

import com.covercloud.shared.response.ApiResponse
import com.covercloud.shared.security.AuthenticationContext
import com.covercloud.user.controller.dto.UserProfileDto
import com.covercloud.user.service.dto.UpdateProfileRequest
import com.covercloud.user.service.UserService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import com.covercloud.user.service.dto.TokenResponse

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userService: UserService,
    private val authenticationContext: AuthenticationContext
) {
    // ...existing code...



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

    @GetMapping("/profiles/bulk")
    fun getUsersByIds(
        @RequestParam ids: List<Long>
    ): ResponseEntity<ApiResponse<List<UserProfileDto>>> {
        return try {
            val users = userService.getUsersByIds(ids).map { user ->
                UserProfileDto(
                    userId = user.id!!,
                    nickname = user.nickname,
                    profileImageUrl = user.profileImage,
                    email = user.email,
                    isDeleted = user.isDeleted
                )
            }
            ResponseEntity.ok(ApiResponse(success = true,data=users))

        } catch (e: Exception) {
            ResponseEntity.status(500).body(
                ApiResponse(success = false, message = "Failed to retrieve user profiles: ${e.message}")
            )
        }
    }

    @GetMapping("/profile")
    fun getUserProfile(
        @RequestParam userId: Long,
    ): ResponseEntity<ApiResponse<UserProfileDto>> { // Map 대신 DTO 권장
        return try {
            val user = userService.getUserById(userId) // 서비스에서 유저 엔티티 혹은 DTO 조회
            val profile = UserProfileDto(
                userId = user.id!!,
                nickname = user.nickname,
                profileImageUrl = user.profileImage,
                email = user.email,
                isDeleted = user.isDeleted
            )

            ResponseEntity.ok(
                ApiResponse(success = true,data=profile)
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(404).body(
                ApiResponse(success = false, message = e.message ?: "User not found")
            )
        } catch (e: Exception) {
            ResponseEntity.status(500).body(
                ApiResponse(success = false, message = "Internal server error")
            )
        }
    }

}

// ========== 테스트 로그인 API ==========
@RestController
@RequestMapping("/api/auth")
class TestLoginController(
    private val userService: UserService
) {
    companion object {
        // 허용된 테스트 계정만 정의
        private val ALLOWED_TEST_ACCOUNTS = mapOf(
            "testuser1" to "Test User 1",
            "testuser2" to "Test User 2",
            "testuser3" to "Test User 3",
            "testuser4" to "Test User 4",
            "testuser5" to "Test User 5"
        )
    }

    /**
     * 테스트 계정 로그인 API (개발/QA 환경용)
     * POST /api/auth/test-login
     * Body: { "socialId": "testuser1" }
     *
     * 허용된 계정: testuser1 ~ testuser5
     *
     * 응답:
     * {
     *   "success": true,
     *   "data": {
     *     "accessToken": "...",
     *     "refreshToken": "...",
     *     "userId": 1,
     *     "nickname": "Test User 1"
     *   }
     * }
     */
    @PostMapping("/test-login")
    fun testLogin(
        @RequestBody request: TestLoginRequest
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        // 허용된 테스트 계정인지 확인
        if (!ALLOWED_TEST_ACCOUNTS.containsKey(request.socialId)) {
            return ResponseEntity.status(400).body(
                ApiResponse(
                    success = false,
                    message = "허용되지 않은 테스트 계정입니다. 다음 계정만 사용 가능합니다: ${ALLOWED_TEST_ACCOUNTS.keys.joinToString(", ")}"
                )
            )
        }

        return try {
            val nickname = ALLOWED_TEST_ACCOUNTS[request.socialId] ?: request.socialId
            val result = userService.testLogin(request.socialId, nickname)
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    data = result,
                    message = "테스트 로그인 성공"
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(400).body(
                ApiResponse(success = false, message = e.message ?: "테스트 로그인 실패")
            )
        }
    }
}

data class TestLoginRequest(
    val socialId: String
)

