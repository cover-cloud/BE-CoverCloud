package com.covercloud.cover.controller

import com.covercloud.cover.controller.dto.CoverRequest
import com.covercloud.cover.service.CoverService
import com.covercloud.cover.service.dto.CoverResponse
import com.covercloud.cover.service.dto.CreateCoverRequest
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

    @PostMapping
    fun saveCover(@RequestBody request: CoverRequest,
                  @RequestParam(required = false) testUserId: Long?): CoverResponse  {
        val coverResponse = coverService.uploadCover(request.toDto(), testUserId)
        return coverResponse
    }
}