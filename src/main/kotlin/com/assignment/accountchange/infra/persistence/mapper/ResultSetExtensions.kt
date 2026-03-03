package com.assignment.accountchange.infra.persistence.mapper

import com.assignment.accountchange.domain.model.EventStatus
import com.assignment.accountchange.domain.model.EventType
import com.assignment.accountchange.domain.model.InboxEvent
import java.sql.ResultSet

fun ResultSet.toInboxEvent(): InboxEvent =
    InboxEvent(
        eventId = getString("event_id"),
        eventType = EventType.from(getString("event_type")),
        accountKey = getString("account_key"),
        payload = getString("payload"),
        status = EventStatus.from(getString("status")),
        errorMessage = getString("error_message"),
        receivedAt = getString("received_at"),
        processedAt = getString("processed_at")
    )