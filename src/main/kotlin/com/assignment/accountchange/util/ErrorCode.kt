package com.assignment.accountchange.util

enum class ErrorCode(
    val code: String,
    val message: String
) {
    INVALID_SIGNATURE("INVALID_SIGNATURE", "Invalid webhook signature"),
    BAD_REQUEST("BAD_REQUEST", "Bad request"),
    NOT_FOUND("NOT_FOUND", "Resource not found"),
    INTERNAL_ERROR("INTERNAL_ERROR", "Internal server error")
}