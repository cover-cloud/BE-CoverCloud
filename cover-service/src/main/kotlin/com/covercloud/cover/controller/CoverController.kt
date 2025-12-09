package com.covercloud.cover.controller

import com.covercloud.cover.controller.dto.CoverRequest
import com.covercloud.cover.service.CoverService
import com.covercloud.cover.service.dto.CoverListResponse
import com.covercloud.cover.service.dto.CoverResponse
import com.covercloud.cover.service.dto.CreateCoverRequest
import com.covercloud.cover.service.dto.PageResponse
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
@RequestMapping("/api/cover")
class CoverController (
    private val coverService: CoverService,
    private val authContext: AuthenticationContext
){

    @PostMapping("/create")
    fun saveCover(
        @RequestBody request: CoverRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<Any>>  {
        val userId = authContext.getCurrentUserId(httpRequest) ?: 1L
        val coverResponse = coverService.uploadCover(request.toDto(), userId)
        return ResponseEntity.ok(ApiResponse(success = true, data = coverResponse))
    }


    @PostMapping("/update")
    fun updateCover(
        @RequestParam coverId: Long,
        @RequestBody request: CoverRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<Any>>  {
        val userId = authContext.getCurrentUserId(httpRequest) ?: 1L
        val coverResponse = coverService.updateCover(coverId, request.toDto(), userId)
        return ResponseEntity.ok(ApiResponse(success = true, data = coverResponse))
    }

    @PostMapping("/delete")
    fun deleteCover(@RequestParam coverId: Long): ResponseEntity<ApiResponse<String>>{
        coverService.deleteCover(coverId)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Cover deleted successfully"))

    }

    @GetMapping("/list")
    fun getCovers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "DESC") sortDirection: String
    ): ResponseEntity<ApiResponse<PageResponse<CoverListResponse>>> {
        val coverList = coverService.getCovers(page, size, sortBy, sortDirection)
        return ResponseEntity.ok(ApiResponse(success = true, data = coverList))
    }

}