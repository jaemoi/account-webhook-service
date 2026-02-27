package com.assignment.accountchange.domain.model

data class InboxEvent(
    val eventId: String,
    val eventType: String,
    val accountKey: String,
    val payload: String,
    val status: EventStatus
)
