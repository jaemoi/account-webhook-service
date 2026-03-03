package com.assignment.accountchange.api.inbox.response

data class ProcessResultResponse(
    val result: String,
    val eventId: String? = null,
    val reason: String? = null
)
