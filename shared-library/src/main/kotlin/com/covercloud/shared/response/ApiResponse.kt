package com.covercloud.shared.response

data class ApiResponse<T>(
    val success: Boolean = true,
    val data: T? = null,
    val message: String? = null

)