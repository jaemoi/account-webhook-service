package com.assignment.accountchange.application.query

import com.assignment.accountchange.domain.model.InboxEvent
import com.assignment.accountchange.infra.persistence.repository.InboxEventRepository
import org.springframework.stereotype.Service

@Service
class InboxEventQueryService(
    private val repository: InboxEventRepository
) {

    fun getEvent(eventId: String): InboxEvent {
        return repository.findByEventId(eventId)
            ?: throw IllegalArgumentException("Event not found: $eventId")
    }
}