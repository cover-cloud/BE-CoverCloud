package com.covercloud.cover.controller

import com.covercloud.cover.controller.dto.CoverRequest
import com.covercloud.cover.domain.TrendingPeriod
import com.covercloud.cover.service.CoverService
import com.covercloud.cover.service.LikeService
import com.covercloud.cover.service.dto.CoverListResponse
import com.covercloud.cover.service.dto.PageResponse
import com.covercloud.cover.service.dto.TrendingCoverResponse
import com.covercloud.shared.response.ApiResponse
import com.covercloud.shared.security.AuthenticationContext
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/cover")
class CoverController (
    private val coverService: CoverService,
    private val likeService: LikeService,
    private val authenticationContext: AuthenticationContext
){

    @PostMapping("/create")
    fun saveCover(
        @RequestBody request: CoverRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<Any>>  {
        val userId = authenticationContext.requireUserId(httpRequest)
        val coverResponse = coverService.uploadCover(request.toDto(), userId)
        return ResponseEntity.ok(ApiResponse(success = true, data = coverResponse))
    }


    @PostMapping("/update")
    fun updateCover(
        @RequestParam coverId: Long,
        @RequestBody request: CoverRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<Any>>  {
        val userId = authenticationContext.requireUserId(httpRequest)
        val coverResponse = coverService.updateCover(coverId, request.toDto())
        return ResponseEntity.ok(ApiResponse(success = true, data = coverResponse))
    }

    @PostMapping("/delete")
    fun deleteCover(@RequestParam coverId: Long): ResponseEntity<ApiResponse<String>>{
        coverService.deleteCover(coverId)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Cover deleted successfully"))

    }

    @GetMapping("/list/{coverId}")
    fun getCover(@PathVariable coverId: Long): ResponseEntity<ApiResponse<CoverListResponse>> {
        val cover = coverService.getCoverById(coverId)
        return ResponseEntity.ok(ApiResponse(success = true, data = cover))
    }

    @GetMapping("/list")
    fun getCovers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "DESC") sortDirection: String,
        @RequestParam(required = false) genre: String?
    ): ResponseEntity<ApiResponse<PageResponse<CoverListResponse>>> {
        val coverList = coverService.getCovers(page, size, sortBy, sortDirection, genre)
        return ResponseEntity.ok(ApiResponse(success = true, data = coverList))
    }

    @GetMapping("/trending")
    fun getTrendingCovers(
        @RequestParam period: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) genre: String?
    ): ResponseEntity<ApiResponse<PageResponse<TrendingCoverResponse>>> {
        val trendingPeriod = when (period.uppercase()) {
            "DAILY" -> TrendingPeriod.DAILY
            "WEEKLY" -> TrendingPeriod.WEEKLY
            "MONTHLY" -> TrendingPeriod.MONTHLY
            else -> throw IllegalArgumentException("Invalid period: $period. Use DAILY, WEEKLY, or MONTHLY")
        }
        
        val trendingCovers = coverService.getTrendingCovers(trendingPeriod, page, size, genre)
        return ResponseEntity.ok(ApiResponse(success = true, data = trendingCovers))
    }


    @GetMapping("/my")
    fun getMyCovers(
        httpRequest: HttpServletRequest,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "DESC") sortDirection: String
    ): ResponseEntity<ApiResponse<PageResponse<CoverListResponse>>> {
        val userId = authenticationContext.requireUserId(httpRequest)
        val coverList = coverService.getCoversByUserId(userId, page, size, sortBy, sortDirection)
        return ResponseEntity.ok(ApiResponse(success = true, data = coverList))
    }

    @PostMapping("/{coverId}/like")
    fun likeCover(
        @PathVariable coverId: Long,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val userId = authenticationContext.requireUserId(httpRequest)
        val success = likeService.like(coverId, userId)
        
        if (success) {
            val newCount = likeService.getLikeCount(coverId)
            return ResponseEntity.ok(ApiResponse(
                success = true, 
                data = mapOf(
                    "liked" to true,
                    "likeCount" to newCount
                )
            ))
        } else {
            return ResponseEntity.ok(ApiResponse(
                success = false, 
                message = "Already liked or cover not found"
            ))
        }
    }

    @PostMapping("/{coverId}/unlike")
    fun unlikeCover(
        @PathVariable coverId: Long,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val userId = authenticationContext.requireUserId(httpRequest)
        val success = likeService.unlike(coverId, userId)
        
        if (success) {
            val newCount = likeService.getLikeCount(coverId)
            return ResponseEntity.ok(ApiResponse(
                success = true, 
                data = mapOf(
                    "liked" to false,
                    "likeCount" to newCount
                )
            ))
        } else {
            return ResponseEntity.ok(ApiResponse(
                success = false, 
                message = "Not liked previously or cover not found"
            ))
        }
    }

    @GetMapping("/{coverId}/like/status")
    fun getLikeStatus(
        @PathVariable coverId: Long,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val userId = authenticationContext.requireUserId(httpRequest)
        val hasLiked = likeService.hasLiked(coverId, userId)
        val likeCount = likeService.getLikeCount(coverId)
        
        return ResponseEntity.ok(ApiResponse(
            success = true,
            data = mapOf(
                "hasLiked" to hasLiked,
                "likeCount" to likeCount
            )
        ))
    }

}
