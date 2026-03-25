package com.covercloud.user.service

import com.covercloud.user.controller.dto.ProfileImageUploadUrlResponse
import com.covercloud.user.domain.User
import com.covercloud.user.service.dto.UpdateProfileRequest
import com.covercloud.user.service.dto.UserInfoResponse
import com.covercloud.user.repository.UserRepository
import com.covercloud.user.service.dto.UploadUrlResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val refreshTokenService: RedisRefreshTokenService,
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


        return mapOf(
            "userInfo" to UserInfoResponse(
                userId = savedUser.id!!,
                nickname = savedUser.nickname,
                profileImage = savedUser.profileImage,
                provider = savedUser.provider.name,
                email = savedUser.email
            )
        )
    }

    @Transactional
    fun deleteAccount(accessToken: String): String {
        val userInfo = authService.getUserInfo(accessToken)
        val userId = userInfo.userId

        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("User not found") }

        // Refresh Token 삭제 (모든 세션 로그아웃)
        refreshTokenService.deleteRefreshToken(userId)

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

    fun getUsersByIds(ids: List<Long>): List<User> {
        if (ids.isEmpty()) return emptyList()

        // findAllById는 기본적으로 제공되는 메서드입니다.
        return userRepository.findAllById(ids)
    }

    fun getUserById(userId: Long): User {
        return userRepository.findById(userId).orElseThrow {
            IllegalArgumentException("User not found")
        }
    }

    /**
     * 테스트 로그인 - socialId로 사용자 찾거나 생성 후 토큰 발급
     */
    @Transactional
    fun testLogin(socialId: String, nickname: String?): Map<String, Any> {
        // 1. 기존 사용자 확인
        var user = userRepository.findBySocialId(socialId)

        // 2. 없으면 새로 생성
        if (user == null) {
            user = User(
                socialId = socialId,
                provider = com.covercloud.user.domain.Provider.TEST,
                nickname = nickname ?: socialId,
                profileImage = "https://via.placeholder.com/150?text=${nickname ?: socialId}",
                email = "$socialId@test.com"
            )
            user = userRepository.save(user)
        }

        // 3. null이 아님을 보장
        user = user ?: throw IllegalArgumentException("Failed to create or find user")

        // 4. 토큰 생성
        val tokens = authService.generateTokens(user.id!!)

        val result: Map<String, Any> = mapOf(
            "userId" to (user.id as Any),
            "socialId" to (user.socialId as Any),
            "nickname" to (user.nickname as Any),
            "accessToken" to (tokens.accessToken as Any),
            "refreshToken" to ((tokens.refreshToken ?: "") as Any),
            "provider" to (user.provider.name as Any)
        )
        return result
    }
}

