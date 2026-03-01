package com.assignment.accountchange.application

import org.springframework.stereotype.Service

@Service
class WebhookFacade {

    fun receive(
        eventId: String,
        signature: String?,
        rawBody: String
    ) {
        println(
            """
            Webhook Received
            eventId=$eventId
            signature=$signature
            body=$rawBody
            """.trimIndent()
        )
    }
}