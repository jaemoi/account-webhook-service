package com.assignment.accountchange.domain.model

data class InboxEvent(
    val eventId: String,
    val eventType: EventType,
    val accountKey: String,
    val payload: String,
    val status: EventStatus,
    val errorMessage: String?,
    val receivedAt: String,
    val processedAt: String?
)
