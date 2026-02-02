package com.covercloud.cover.controller

import com.covercloud.cover.controller.dto.CoverReportRequest
import com.covercloud.cover.service.CoverReportService
import com.covercloud.cover.service.dto.CoverReportResponse
import com.covercloud.shared.response.ApiResponse
import com.covercloud.shared.security.AuthenticationContext
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/cover/report")
class CoverReportController(
    private val coverReportService: CoverReportService,
    private val authenticationContext: AuthenticationContext
) {

    @PostMapping
    fun reportCover(
        @RequestBody request: CoverReportRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<CoverReportResponse>> {
        return try {
            val userId = authenticationContext.requireUserId(httpRequest)

            val response = coverReportService.reportCover(
                coverId = request.coverId,
                reason = request.reason,
                description = request.description
            )

            ResponseEntity.ok(ApiResponse(
                success = true,
                data = response,
                message = "Cover reported successfully"
            ))
        } catch (e: Exception) {
            // ✅ 에러 타입별 상태 코드 반환
            when {
                // 중복 신고 → 400 Bad Request
                e.message?.contains("You have already reported") == true -> {
                    ResponseEntity.status(400).body(ApiResponse(
                        success = false,
                        message = e.message ?: "You have already reported this cover"
                    ))
                }
                // 유효하지 않은 신고 사유 → 400 Bad Request
                e.message?.contains("Invalid reason") == true -> {
                    ResponseEntity.status(400).body(ApiResponse(
                        success = false,
                        message = e.message ?: "Invalid report reason"
                    ))
                }
                // 커버를 찾을 수 없음 → 404 Not Found
                e.message?.contains("Cover not found") == true ||
                e.message?.contains("404") == true -> {
                    ResponseEntity.status(404).body(ApiResponse(
                        success = false,
                        message = "Cover not found"
                    ))
                }
                // 토큰 만료 또는 기타 에러 → 401 Unauthorized
                else -> ResponseEntity.status(401).body(ApiResponse(
                    success = false,
                    message = "Invalid or expired access token"
                ))
            }
        }
    }

    @GetMapping
    fun getCoverReportInfo(
        @RequestParam coverId: Long,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<CoverReportResponse>> {
        return try {
            authenticationContext.requireUserId(httpRequest)

            val response = coverReportService.getCoverReportInfo(coverId)
            ResponseEntity.ok(ApiResponse(
                success = true,
                data = response,
                message = "Cover report info retrieved successfully"
            ))
        } catch (e: Exception) {
            when {
                e.message?.contains("404") == true -> {
                    ResponseEntity.status(404).body(ApiResponse(
                        success = false,
                        message = "Cover not found"
                    ))
                }
                else -> ResponseEntity.status(401).body(ApiResponse(
                    success = false,
                    message = "Invalid or expired access token"
                ))
            }
        }
    }
}
