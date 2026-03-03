package com.assignment.accountchange.api.inbox.response

data class InboxEventResponse(
    val eventId: String,
    val eventType: String,
    val accountKey: String,
    val status: String,
    val errorMessage: String?,
    val receivedAt: String,
    val processedAt: String?
)
