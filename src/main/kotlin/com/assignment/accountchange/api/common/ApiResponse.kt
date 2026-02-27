package com.assignment.accountchange.api.common

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorResponse? = null
) {
    companion object {
        fun <T> ok(data: T? = null): ApiResponse<T> =
            ApiResponse(success = true, data = data)

        fun fail(code: String, message: String): ApiResponse<Nothing> =
            ApiResponse(success = false, error = ErrorResponse(code, message))
    }
}
