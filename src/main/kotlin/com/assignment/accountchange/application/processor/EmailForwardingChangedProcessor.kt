package com.assignment.accountchange.application.processor

import com.assignment.accountchange.domain.model.EventType
import com.assignment.accountchange.domain.model.InboxEvent
import com.assignment.accountchange.infra.persistence.repository.AccountRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class EmailForwardingChangedProcessor(
    private val objectMapper: ObjectMapper,
    private val accountRepository: AccountRepository
) : EventProcessor {

    override val supportedType =
        EventType.EMAIL_FORWARDING_CHANGED

    override fun process(event: InboxEvent) {
        val json = objectMapper.readTree(event.payload)
        val email = json["email"]?.asText()
        accountRepository.upsertEmail(event.accountKey, email)
    }
}