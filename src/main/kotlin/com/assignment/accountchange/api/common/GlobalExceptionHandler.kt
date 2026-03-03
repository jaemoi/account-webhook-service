package com.assignment.accountchange.api.common

import com.assignment.accountchange.application.exception.ForbiddenException
import com.assignment.accountchange.application.exception.UnauthorizedException
import com.fasterxml.jackson.core.JsonProcessingException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger =
        LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /**
     * 잘못된 요청 파라미터
     */
    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgument(e: IllegalArgumentException): ApiResponse<Nothing> {
        return ApiResponse.fail(
            code = "BAD_REQUEST",
            message = e.message ?: "bad request"
        )
    }

    /**
     * Webhook payload JSON 파싱 실패
     */
    @ExceptionHandler(JsonProcessingException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidJson(e: JsonProcessingException): ApiResponse<Nothing> {

        logger.warn("Invalid JSON payload", e)

        return ApiResponse.fail(
            code = "INVALID_PAYLOAD",
            message = "Invalid webhook payload"
        )
    }

    /**
     * 인증 실패 (signature 없음 등)
     */
    @ExceptionHandler(UnauthorizedException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleUnauthorized(
        e: UnauthorizedException
    ): ApiResponse<Nothing> {

        return ApiResponse.fail(
            code = "UNAUTHORIZED",
            message = e.message ?: "unauthorized"
        )
    }

    /**
     * 권한 실패 (signature 검증 실패)
     */
    @ExceptionHandler(ForbiddenException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleForbidden(
        e: ForbiddenException
    ): ApiResponse<Nothing> {

        return ApiResponse.fail(
            code = "FORBIDDEN",
            message = e.message ?: "forbidden"
        )
    }

    /**
     * 예상 못한 서버 에러 (내부 정보 노출 금지)
     */
    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleException(e: Exception)
            : ApiResponse<Nothing> {
        logger.error("Unhandled server error", e)
        return ApiResponse.fail(
            code = "INTERNAL_ERROR",
            message = "Unexpected server error"
        )
    }
}