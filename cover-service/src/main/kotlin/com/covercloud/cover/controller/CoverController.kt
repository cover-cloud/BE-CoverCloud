package com.covercloud.cover.controller

import com.covercloud.cover.controller.dto.CoverRequest
import com.covercloud.cover.controller.dto.SearchSort
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
import com.covercloud.cover.controller.dto.TrendingRequest
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
        return try {
            val userId = authenticationContext.requireUserId(httpRequest)
            val coverResponse = coverService.uploadCover(request.toDto(), userId)
            ResponseEntity.ok(ApiResponse(success = true, data = coverResponse))
        } catch (e: Exception) {
            ResponseEntity.status(401).body(ApiResponse(success = false, message = "Invalid or expired access token"))
        }
    }


    @PostMapping("/update")
    fun updateCover(
        @RequestParam coverId: Long,
        @RequestBody request: CoverRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<Any>>  {
        return try {
            val userId = authenticationContext.requireUserId(httpRequest)
            val coverResponse = coverService.updateCover(coverId, request.toDto())
            ResponseEntity.ok(ApiResponse(success = true, data = coverResponse))
        } catch (e: Exception) {
            ResponseEntity.status(401).body(ApiResponse(success = false, message = "Invalid or expired access token"))
        }
    }

    @PostMapping("/delete")
    fun deleteCover(
        @RequestParam coverId: Long,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<String>> {
        return try {
            val userId = authenticationContext.requireUserId(httpRequest)
            coverService.deleteCover(coverId)
            ResponseEntity.ok(ApiResponse(success = true, message = "Cover deleted successfully"))
        } catch (e: Exception) {
            ResponseEntity.status(401).body(ApiResponse(success = false, message = "Invalid or expired access token"))
        }
    }

    @GetMapping("/list/{coverId}")
    fun getCover(
        @PathVariable coverId: Long,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<CoverListResponse>> {
        val userId = try { authenticationContext.requireUserId(httpRequest) } catch (e: Exception) { null }
        val cover = coverService.getCoverById(coverId, userId)
        return ResponseEntity.ok(ApiResponse(success = true, data = cover))
    }

    @GetMapping("/list")
    fun getCovers(
        @RequestBody req: TrendingRequest,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<PageResponse<CoverListResponse>>> {
        val trendingPeriod = when (req.period?.uppercase()) {
            "DAILY" -> TrendingPeriod.DAILY
            "WEEKLY" -> TrendingPeriod.WEEKLY
            "MONTHLY" -> TrendingPeriod.MONTHLY
            null -> null  // period가 없으면 전체
            else -> throw IllegalArgumentException("Invalid period: ${req.period}. Use DAILY, WEEKLY, or MONTHLY")
        }
        val userId = try { authenticationContext.requireUserId(httpRequest) } catch (e: Exception) { null }
        val coverList = coverService.getCovers(trendingPeriod,req.page, req.size, req.genres, userId)
        return ResponseEntity.ok(ApiResponse(success = true, data = coverList))
    }


    @GetMapping("/search/title")
    fun searchCoversByTitle(
        @RequestParam title: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "LATEST") sortBy: SearchSort,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<PageResponse<CoverListResponse>>> {
        val userId = try { authenticationContext.requireUserId(httpRequest) } catch (e: Exception) { null }
        val searchResult = coverService.searchCoversByTitle(title, page, size, sortBy, userId)
        return ResponseEntity.ok(ApiResponse(success = true, data = searchResult))
    }

    @GetMapping("/search/tags")
    fun searchCoversByTags(
        @RequestParam tags: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "LATEST") sortBy: SearchSort,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<PageResponse<CoverListResponse>>> {
        val userId = try { authenticationContext.requireUserId(httpRequest) } catch (e: Exception) { null }
        val searchResult = coverService.searchCoversByTags(tags, page, size, sortBy, userId)
        return ResponseEntity.ok(ApiResponse(success = true, data = searchResult))
    }


    @PostMapping("/trending/search")
    fun searchTrending(
        @RequestBody req: TrendingRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<PageResponse<TrendingCoverResponse>>> {
        val userId = try { authenticationContext.requireUserId(httpRequest) } catch (e: Exception) { null }
        val trendingPeriod = when (req.period?.uppercase()) {
            "DAILY" -> TrendingPeriod.DAILY
            "WEEKLY" -> TrendingPeriod.WEEKLY
            "MONTHLY" -> TrendingPeriod.MONTHLY
            null -> null  // period가 없으면 전체
            else -> throw IllegalArgumentException("Invalid period: ${req.period}. Use DAILY, WEEKLY, or MONTHLY")
        }

        val trendingCovers = coverService.getTrendingCovers(trendingPeriod, req.page, req.size, req.genres, userId)
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
        return try {
            val userId = authenticationContext.requireUserId(httpRequest)
            val coverList = coverService.getCoversByUserId(userId, page, size, sortBy, sortDirection)
            ResponseEntity.ok(ApiResponse(success = true, data = coverList))
        } catch (e: Exception) {
            ResponseEntity.status(401).body(ApiResponse(success = false, message = "Invalid or expired access token"))
        }
    }

    @PostMapping("/{coverId}/like")
    fun likeCover(
        @PathVariable coverId: Long,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        return try {
            val userId = authenticationContext.requireUserId(httpRequest)
            val success = likeService.like(coverId, userId)

            if (success) {
                val newCount = likeService.getLikeCount(coverId)
                ResponseEntity.ok(ApiResponse(
                    success = true,
                    data = mapOf(
                        "liked" to true,
                        "likeCount" to newCount
                    )
                ))
            } else {
                ResponseEntity.ok(ApiResponse(
                    success = false,
                    message = "Already liked or cover not found"
                ))
            }
        } catch (e: Exception) {
            ResponseEntity.status(401).body(ApiResponse(success = false, message = "Invalid or expired access token"))
        }
    }

    @PostMapping("/{coverId}/unlike")
    fun unlikeCover(
        @PathVariable coverId: Long,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        return try {
            val userId = authenticationContext.requireUserId(httpRequest)
            val success = likeService.unlike(coverId, userId)

            if (success) {
                val newCount = likeService.getLikeCount(coverId)
                ResponseEntity.ok(ApiResponse(
                    success = true,
                    data = mapOf(
                        "liked" to false,
                        "likeCount" to newCount
                    )
                ))
            } else {
                ResponseEntity.ok(ApiResponse(
                    success = false,
                    message = "Not liked previously or cover not found"
                ))
            }
        } catch (e: Exception) {
            ResponseEntity.status(401).body(ApiResponse(success = false, message = "Invalid or expired access token"))
        }
    }

    @GetMapping("/{coverId}/like/status")
    fun getLikeStatus(
        @PathVariable coverId: Long,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        return try {
            val userId = authenticationContext.requireUserId(httpRequest)
            val hasLiked = likeService.hasLiked(coverId, userId)
            val likeCount = likeService.getLikeCount(coverId)

            ResponseEntity.ok(ApiResponse(
                success = true,
                data = mapOf(
                    "hasLiked" to hasLiked,
                    "likeCount" to likeCount
                )
            ))
        } catch (e: Exception) {
            ResponseEntity.status(401).body(ApiResponse(success = false, message = "Invalid or expired access token"))
        }
    }

    // ✅ 사용자가 댓글을 단 커버들 조회
    @GetMapping("/my/comments")
    fun getCoversByUserComments(
        httpRequest: HttpServletRequest,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "DESC") sortDirection: String
    ): ResponseEntity<ApiResponse<PageResponse<CoverListResponse>>> {
        return try {
            val userId = authenticationContext.requireUserId(httpRequest)
            val coverList = coverService.getCoversByUserComments(userId, page, size, sortBy, sortDirection)
            ResponseEntity.ok(ApiResponse(success = true, data = coverList))
        } catch (e: Exception) {
            ResponseEntity.status(401).body(ApiResponse(success = false, message = "Invalid or expired access token"))
        }
    }

}
