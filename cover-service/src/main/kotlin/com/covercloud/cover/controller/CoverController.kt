package com.covercloud.cover.controller

import com.covercloud.cover.controller.dto.CoverRequest
import com.covercloud.cover.service.CoverService
import com.covercloud.cover.service.dto.CoverResponse
import com.covercloud.cover.service.dto.CreateCoverRequest
import com.covercloud.shared.response.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/cover")
class CoverController (
    private val coverService: CoverService
){

    @PostMapping("/create")
    fun saveCover(@RequestBody request: CoverRequest,
                  @RequestParam(required = false) testUserId: Long?): ResponseEntity<ApiResponse<Any>>  {
        val coverResponse = coverService.uploadCover(request.toDto(), testUserId)
        return ResponseEntity.ok(ApiResponse(success = true, data = coverResponse))
    }


    @PostMapping("/update")
    fun updateCover(@RequestParam coverId: Long,
                    @RequestBody request: CoverRequest,
                  @RequestParam(required = false) testUserId: Long?): ResponseEntity<ApiResponse<Any>>  {
        val coverResponse = coverService.updateCover(coverId, request.toDto(), testUserId)
        return ResponseEntity.ok(ApiResponse(success = true, data = coverResponse))
    }

    @PostMapping("/delete")
    fun deleteCover(@RequestParam coverId: Long): ResponseEntity<ApiResponse<String>>{
        coverService.deleteCover(coverId)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Cover deleted successfully"))

    }
}