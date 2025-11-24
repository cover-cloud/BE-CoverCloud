package com.covercloud.shared

data class ApiResponse<T>(
    val success: Boolean = true,
    val data: T? = null,
    val message: String? = null

)
