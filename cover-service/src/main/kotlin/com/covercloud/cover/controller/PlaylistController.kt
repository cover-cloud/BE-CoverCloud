package com.covercloud.cover.controller

import com.covercloud.cover.controller.dto.AddPlaylistItemRequest
import com.covercloud.cover.controller.dto.CreatePlaylistRequest
import com.covercloud.cover.controller.dto.ReorderPlaylistItemsRequest
import com.covercloud.cover.controller.dto.UpdatePlaylistNameRequest
import com.covercloud.cover.service.PlaylistService
import com.covercloud.shared.response.ApiResponse
import com.covercloud.shared.security.AuthenticationContext
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/playlist")
class PlaylistController(
    private val playlistService: PlaylistService,
    private val authenticationContext: AuthenticationContext,
) {

    // GET /me/playlists
    @GetMapping("/me")
    fun getMyPlaylists(httpRequest: HttpServletRequest): ResponseEntity<ApiResponse<Any>> {
        return try {
            val userId = authenticationContext.requireUserId(httpRequest)
            val playlists = playlistService.getMyPlaylists(userId)
            ResponseEntity.ok(ApiResponse(success = true, data = playlists))
        } catch (e: IllegalStateException) {
            ResponseEntity.status(401).body(ApiResponse(success = false, message = "Invalid or expired access token"))
        }
    }

    // GET /playlists/{playlistId}?includeItems=true
    @GetMapping("/{playlistId}")
    fun getPlaylist(
        @PathVariable playlistId: Long,
        @RequestParam(defaultValue = "false") includeItems: Boolean,
    ): ResponseEntity<ApiResponse<Any>> {
        return try {
            val detail = playlistService.getPlaylist(playlistId, includeItems)
            ResponseEntity.ok(ApiResponse(success = true, data = detail))
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(404).body(ApiResponse(success = false, message = e.message))
        }
    }

    // POST /playlists
    @PostMapping
    fun createPlaylist(
        @RequestBody request: CreatePlaylistRequest,
        httpRequest: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Any>> {
        return try {
            val userId = authenticationContext.requireUserId(httpRequest)
            val playlist = playlistService.createPlaylist(userId, request.name)
            ResponseEntity.ok(ApiResponse(success = true, data = playlist))
        } catch (e: IllegalStateException) {
            ResponseEntity.status(401).body(ApiResponse(success = false, message = "Invalid or expired access token"))
        }
    }

    // 이름 수정: PATCH /playlists/{playlistId}
    @PatchMapping("/{playlistId}")
    fun updatePlaylistName(
        @PathVariable playlistId: Long,
        @RequestBody request: UpdatePlaylistNameRequest,
        httpRequest: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Any>> {
        return try {
            val userId = authenticationContext.requireUserId(httpRequest)
            playlistService.updatePlaylistName(playlistId, userId, request.name)
            ResponseEntity.ok(ApiResponse(success = true, message = "Playlist name updated"))
        } catch (e: IllegalStateException) {
            ResponseEntity.status(401).body(ApiResponse(success = false, message = "Invalid or expired access token"))
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(404).body(ApiResponse(success = false, message = e.message))
        }
    }

    // 삭제: DELETE /playlists/{playlistId}
    @DeleteMapping("/{playlistId}")
    fun deletePlaylist(
        @PathVariable playlistId: Long,
        httpRequest: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Any>> {
        return try {
            val userId = authenticationContext.requireUserId(httpRequest)
            playlistService.deletePlaylist(playlistId, userId)
            ResponseEntity.ok(ApiResponse(success = true, message = "Playlist deleted"))
        } catch (e: IllegalStateException) {
            ResponseEntity.status(401).body(ApiResponse(success = false, message = "Invalid or expired access token"))
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(404).body(ApiResponse(success = false, message = e.message))
        }
    }

    // POST /playlists/{playlistId}/items
    @PostMapping("/{playlistId}/items")
    fun addItem(
        @PathVariable playlistId: Long,
        @RequestBody request: AddPlaylistItemRequest,
        httpRequest: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Any>> {
        return try {
            val userId = authenticationContext.requireUserId(httpRequest)
            val item = playlistService.addItem(playlistId, userId, request.coverId)
            ResponseEntity.ok(ApiResponse(success = true, data = item))
        } catch (e: IllegalStateException) {
            ResponseEntity.status(400).body(ApiResponse(success = false, message = e.message))
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(404).body(ApiResponse(success = false, message = e.message))
        }
    }

    // DELETE /playlists/{playlistId}/items/{itemId}
    @DeleteMapping("/{playlistId}/items/{itemId}")
    fun removeItem(
        @PathVariable playlistId: Long,
        @PathVariable itemId: Long,
        httpRequest: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Any>> {
        return try {
            val userId = authenticationContext.requireUserId(httpRequest)
            playlistService.removeItem(playlistId, userId, itemId)
            ResponseEntity.ok(ApiResponse(success = true, message = "Item removed from playlist"))
        } catch (e: IllegalStateException) {
            ResponseEntity.status(401).body(ApiResponse(success = false, message = "Invalid or expired access token"))
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(404).body(ApiResponse(success = false, message = e.message))
        }
    }

    // POST /playlists/{playlistId}/items/reorder
    @PostMapping("/{playlistId}/items/reorder")
    fun reorderItems(
        @PathVariable playlistId: Long,
        @RequestBody request: ReorderPlaylistItemsRequest,
        httpRequest: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Any>> {
        return try {
            val userId = authenticationContext.requireUserId(httpRequest)
            playlistService.reorderItems(playlistId, userId, request.orderedItemIds)
            ResponseEntity.ok(ApiResponse(success = true, message = "Playlist reordered"))
        } catch (e: IllegalStateException) {
            ResponseEntity.status(401).body(ApiResponse(success = false, message = "Invalid or expired access token"))
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(404).body(ApiResponse(success = false, message = e.message))
        }
    }
}
