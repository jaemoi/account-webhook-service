package com.assignment.accountchange.application

import com.assignment.accountchange.application.exception.ForbiddenException
import com.assignment.accountchange.application.exception.UnauthorizedException
import com.assignment.accountchange.domain.model.EventStatus
import com.assignment.accountchange.domain.model.EventType
import com.assignment.accountchange.domain.security.HmacVerifier
import com.assignment.accountchange.infra.persistence.repository.AccountRepository
import com.assignment.accountchange.infra.persistence.repository.InboxEventRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WebhookFacade(
    private val inboxEventRepository: InboxEventRepository,
    private val accountRepository: AccountRepository,
    private val objectMapper: ObjectMapper,
    private val hmacVerifier: HmacVerifier
) {

    private val logger = LoggerFactory.getLogger(WebhookFacade::class.java)

    @Transactional
    fun receive(
        eventId: String,
        signature: String?,
        rawBody: String
    ): WebhookHandleResult {

        validateHeaders(eventId, signature, rawBody)

        val json = objectMapper.readTree(rawBody)

        val eventType =
            EventType.from(json["eventType"].asText())

        val accountKey =
            json["accountKey"].asText()

        try {
            inboxEventRepository.insert(
                eventId = eventId,
                eventType = eventType,
                accountKey = accountKey,
                payload = rawBody
            )

            return WebhookHandleResult.Accepted
        } catch (e: DataAccessException) {
            if (isDuplicateKey(e)) {
                return handleDuplicate(eventId)
            }
            throw e
        }
    }

    private fun validateHeaders(
        eventId: String,
        signature: String?,
        rawBody: String
    ) {
        if (eventId.isBlank()) {
            throw UnauthorizedException("Missing event id")
        }

        if (signature.isNullOrBlank()) {
            throw UnauthorizedException("Missing signature")
        }

        if (!hmacVerifier.verify(rawBody, signature)) {
            logger.warn("Invalid webhook signature. eventId={}", eventId)
            throw ForbiddenException("Invalid signature")
        }
    }

    private fun handleDuplicate(eventId: String): WebhookHandleResult {

        val status =
            inboxEventRepository.findStatusByEventId(eventId)

        return when (status) {
            EventStatus.RECEIVED,
            EventStatus.PROCESSING ->
                WebhookHandleResult.InProgress

            EventStatus.DONE ->
                WebhookHandleResult.AlreadyProcessed

            EventStatus.FAILED,
            null ->
                WebhookHandleResult.Accepted
        }
    }

    private fun isDuplicateKey(e: Throwable): Boolean {
        var cause: Throwable? = e
        while (cause != null) {
            if (cause is org.sqlite.SQLiteException) {
                val code = cause.resultCode
                if (
                    code == org.sqlite.SQLiteErrorCode.SQLITE_CONSTRAINT ||
                    code == org.sqlite.SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE ||
                    code == org.sqlite.SQLiteErrorCode.SQLITE_CONSTRAINT_PRIMARYKEY
                ) return true

                // 드라이버/버전별로 code가 다르게 올라오는 경우 마지막 안전장치
                if (cause.message?.contains(
                        "UNIQUE constraint failed",
                        ignoreCase = true
                    ) == true
                ) {
                    return true
                }
            }
            cause = cause.cause
        }
        return false
    }
}