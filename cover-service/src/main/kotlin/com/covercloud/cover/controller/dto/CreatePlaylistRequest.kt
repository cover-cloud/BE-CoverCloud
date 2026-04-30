package com.covercloud.cover.controller.dto

data class CreatePlaylistRequest(
    val name: String,
)

data class UpdatePlaylistNameRequest(
    val name: String,
)

data class AddPlaylistItemRequest(
    val coverId: Long,
)

data class ReorderPlaylistItemsRequest(
    val orderedItemIds: List<Long>,
)
