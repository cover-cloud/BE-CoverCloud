package com.covercloud.cover.controller

import com.covercloud.cover.controller.dto.CommentRequest
import com.covercloud.cover.controller.dto.UpdateCommentRequest
import com.covercloud.cover.service.CommentService
import com.covercloud.shared.response.ApiResponse
import com.covercloud.shared.security.AuthenticationContext
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/comment")
class CommentController(
    private val commentService: CommentService,
    private val authenticationContext: AuthenticationContext
) {
    @PostMapping("/create")
    fun saveComment(
        @RequestBody request:CommentRequest,
        httpRequest: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Any>>{
        val userId = authenticationContext.requireUserId(httpRequest)
        val commentResponse = commentService.addComment(request.toDto(), userId)
        return ResponseEntity.ok(ApiResponse(success = true, data = commentResponse))
    }

    @PostMapping("/update")
    fun updateComment(
        @RequestParam commentId: Long,
        @RequestBody request:UpdateCommentRequest,
        httpRequest: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Any>>{
        val userId = authenticationContext.requireUserId(httpRequest)
        val commentResponse = commentService.updateComment(commentId, request.toDto(), userId)
        return ResponseEntity.ok(ApiResponse(success = true, data = commentResponse))
    }

}