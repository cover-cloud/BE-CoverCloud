package com.covercloud.user.service

import com.covercloud.user.controller.dto.ProfileImageUploadUrlResponse
import com.covercloud.user.service.dto.UpdateProfileRequest
import com.covercloud.user.service.dto.UserInfoResponse
import com.covercloud.user.repository.UserRepository
import com.covercloud.user.repository.RefreshTokenRepository
import com.covercloud.user.service.dto.UploadUrlResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val gcsSignedUrlService: GcsSignedUrlService,
    private val authService: AuthService
) {

    @Transactional
    fun updateProfile(accessToken: String, request: UpdateProfileRequest): Map<String, Any?> {
        val userInfo = authService.getUserInfo(accessToken)
        val userId = userInfo.userId

        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("User not found") }

        val finalProfileImageUrl = request.profileImage?.let { imagePath ->
            if (imagePath.startsWith("http")) {
                imagePath // 이미 전체 URL인 경우 그대로 사용
            } else {
                // GCS 버킷 도메인 결합 (버킷명은 본인의 것으로 수정하세요)
                "https://storage.googleapis.com/covercloud-bucket/$imagePath"
            }
        }

        // 닉네임과 프로필사진만 업데이트 (소셜로그인이라 이메일은 변경 불가)
        user.updateProfile(request.nickname ?: user.nickname,
            finalProfileImageUrl ?: user.profileImage)

        val savedUser = userRepository.save(user)

        // 닉네임 변경 후 새로운 토큰 발급
        val newTokens = authService.generateTokens(userId)

        return mapOf(
            "userInfo" to UserInfoResponse(
                userId = savedUser.id!!,
                nickname = savedUser.nickname,
                profileImage = savedUser.profileImage,
                provider = savedUser.provider.name,
                email = savedUser.email
            ),
            "accessToken" to newTokens.accessToken,
            "refreshToken" to newTokens.refreshToken
        )
    }

    @Transactional
    fun deleteAccount(accessToken: String): String {
        val userInfo = authService.getUserInfo(accessToken)
        val userId = userInfo.userId

        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("User not found") }

        // Refresh Token 삭제 (모든 세션 로그아웃)
        refreshTokenRepository.deleteByUserId(userId)

        // 소프트 삭제 - 사용자 상태를 deleted로 변경
        user.delete()
        userRepository.save(user)

        return "Account deleted successfully"
    }

    fun createProfileImageUploadUrl(
        userId: Long,
        contentType: String
    ): UploadUrlResponse {
        return gcsSignedUrlService.createProfileUploadUrl(
            userId = userId,
            contentType = contentType
        )
    }

}
