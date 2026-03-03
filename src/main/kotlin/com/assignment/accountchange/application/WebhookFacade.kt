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
import org.springframework.dao.DuplicateKeyException
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



        try {

            inboxEventRepository.insert(
                eventId = eventId,
                eventType = EventType.UNKNOWN, // 임시값
                accountKey = "",
                payload = rawBody
            )

            val json = objectMapper.readTree(rawBody)

            val eventType =
                EventType.from(json["eventType"].asText())

            val accountKey =
                json["accountKey"].asText()


            if (eventType == EventType.ACCOUNT_DELETED) {
                accountRepository.markDeleted(accountKey)
            }

            logger.info("Webhook event saved. eventId={}", eventId)
            return WebhookHandleResult.Accepted
        } catch (e: DuplicateKeyException) {

            return handleDuplicate(eventId)
        } catch (e: DataAccessException) {

            if (isDuplicateKey(e)) {
                return handleDuplicate(eventId)
            }

            inboxEventRepository.markFailed(eventId, e.message)
            throw e
        } catch (e: Exception) {

            inboxEventRepository.markFailed(
                eventId,
                e.message
            )

            throw e
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

    private fun isDuplicateKey(e: DataAccessException): Boolean {
        val root = e.rootCause
        return root is org.sqlite.SQLiteException &&
                root.resultCode == org.sqlite.SQLiteErrorCode.SQLITE_CONSTRAINT
    }
}