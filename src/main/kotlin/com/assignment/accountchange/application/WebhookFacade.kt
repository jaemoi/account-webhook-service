package com.assignment.accountchange.application

import com.assignment.accountchange.application.exception.UnauthorizedException
import com.assignment.accountchange.domain.model.EventStatus
import com.assignment.accountchange.domain.model.EventType
import com.assignment.accountchange.domain.security.HmacVerifier
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
            throw UnauthorizedException("Invalid signature")
        }

        val json = objectMapper.readTree(rawBody)

        val eventType = EventType.from(json["eventType"].asText())
        val accountKey = json["accountKey"].asText()

        return try {

            inboxEventRepository.insert(
                eventId = eventId,
                eventType = eventType,
                accountKey = accountKey,
                payload = rawBody
            )

            println("Event saved: $eventId")
            WebhookHandleResult.Accepted
        } catch (e: DuplicateKeyException) {

            handleDuplicate(eventId)
        } catch (e: DataAccessException) {
            // SQLite fallback (VERY IMPORTANT)
            if (e.message?.contains("UNIQUE constraint failed") == true) {
                handleDuplicate(eventId)
            } else {
                throw e
            }
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
}