package com.covercloud.cover.infrastructure.feign

import com.covercloud.cover.infrastructure.dto.UserProfileDto
import com.covercloud.shared.response.ApiResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(name = "user-service", url = "\${service.user.url}")
interface UserClient {
    @GetMapping("/api/user/profile/{userId}")
    fun getUserProfile(@PathVariable userId: Long): ApiResponse<UserProfileDto>
}

