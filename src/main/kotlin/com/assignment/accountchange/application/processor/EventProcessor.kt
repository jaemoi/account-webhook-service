package com.assignment.accountchange.application.processor

import com.assignment.accountchange.domain.model.InboxEvent

interface EventProcessor {
    fun supports(eventType: String): Boolean
    fun process(event: InboxEvent)
}