package com.assignment.accountchange.application.processor

import com.assignment.accountchange.domain.model.EventType
import com.assignment.accountchange.domain.model.InboxEvent
import com.assignment.accountchange.infra.persistence.repository.SocialAccountRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class SocialAccountDeletedProcessor(
    private val objectMapper: ObjectMapper,
    private val socialAccountRepository: SocialAccountRepository
) : EventProcessor {

    override val supportedType =
        EventType.SOCIAL_ACCOUNT_DELETED

    override fun process(event: InboxEvent) {

        val json = objectMapper.readTree(event.payload)

        val provider =
            json["provider"].asText()

        socialAccountRepository
            .markProviderDeleted(event.accountKey, provider)
    }
}