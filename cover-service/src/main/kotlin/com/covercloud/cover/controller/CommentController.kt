package com.covercloud.cover.controller

import com.covercloud.cover.controller.dto.CommentRequest
import com.covercloud.cover.controller.dto.UpdateCommentRequest
import com.covercloud.cover.service.CommentService
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
@RequestMapping("/api/cover/comment")
class CommentController(
    private val commentService: CommentService,
    private val authenticationContext: AuthenticationContext
) {
    @PostMapping("/create")
    fun saveComment(
        @RequestBody request:CommentRequest,
        httpRequest: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Any>>{
        return try {
            val userId = authenticationContext.requireUserId(httpRequest)
            val commentResponse = commentService.addComment(request.toDto(), userId)
            ResponseEntity.ok(ApiResponse(success = true, data = commentResponse))
        } catch (e: Exception) {
            ResponseEntity.status(401).body(ApiResponse(success = false, message = "Invalid or expired access token"))
        }
    }

    @PostMapping("/update")
    fun updateComment(
        @RequestParam commentId: Long,
        @RequestBody request:UpdateCommentRequest,
        httpRequest: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Any>>{
        return try {
            val userId = authenticationContext.requireUserId(httpRequest)
            val commentResponse = commentService.updateComment(commentId, request.toDto(), userId)
            ResponseEntity.ok(ApiResponse(success = true, data = commentResponse))
        } catch (e: Exception) {
            ResponseEntity.status(401).body(ApiResponse(success = false, message = "Invalid or expired access token"))
        }
    }

    @PostMapping("/delete")
    fun deleteComment(
        @RequestParam commentId: Long,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<String>> {
        return try {
            val userId = authenticationContext.requireUserId(httpRequest)
            commentService.deleteComment(commentId, userId)
            ResponseEntity.ok(ApiResponse(success = true, message = "Comment deleted successfully"))
        } catch (e: Exception) {
            ResponseEntity.status(401).body(ApiResponse(success = false, message = "Invalid or expired access token"))
        }
    }

    @GetMapping("/my")
    fun getMyComments(
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<Any>> {
        return try {
            val userId = authenticationContext.requireUserId(httpRequest)
            val comments = commentService.getCommentsByUserId(userId)
            ResponseEntity.ok(ApiResponse(success = true, data = comments))
        } catch (e: Exception) {
            ResponseEntity.status(401).body(ApiResponse(success = false, message = "Invalid or expired access token"))
        }
    }

    @PostMapping("/like")
    fun toggleLike(
        @RequestParam commentId: Long,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<Any>> {
        return try {
            val userId = authenticationContext.requireUserId(httpRequest)
            val commentResponse = commentService.toggleLike(commentId, userId)
            ResponseEntity.ok(ApiResponse(success = true, data = commentResponse))
        } catch (e: Exception) {
            ResponseEntity.status(401).body(ApiResponse(success = false, message = "Invalid or expired access token"))
        }
    }

}