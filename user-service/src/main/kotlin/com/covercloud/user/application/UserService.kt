package com.covercloud.user.application

import com.covercloud.shared.response.ApiResponse
import com.covercloud.user.application.dto.UpdateProfileRequest
import com.covercloud.user.application.dto.UserInfoResponse
import com.covercloud.user.infrastructure.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val authService: AuthService
) {

    @Transactional
    fun updateProfile(accessToken: String, request: UpdateProfileRequest): UserInfoResponse {
        val userInfo = authService.getUserInfo(accessToken)
        val userId = userInfo.userId

        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("User not found") }

        // 닉네임과 프로필사진만 업데이트 (소셜로그인이라 이메일은 변경 불가)
        user.updateProfile(request.nickname, request.profileImage)

        val savedUser = userRepository.save(user)

        return UserInfoResponse(
            userId = savedUser.id!!,
            nickname = savedUser.nickname,
            profileImage = savedUser.profileImage,
            provider = savedUser.provider.name,
            email = savedUser.email
        )
    }
}
