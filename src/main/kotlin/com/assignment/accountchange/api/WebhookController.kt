package com.assignment.accountchange.api

import com.assignment.accountchange.application.WebhookFacade
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/webhooks")
class WebhookController(
    private val webhookFacade: WebhookFacade
) {

    /**
     * POST /webhooks/account-changes
     */
    @PostMapping("/account-changes")
    fun receiveWebhook(
        @RequestHeader("X-Event-Id") eventId: String,
        @RequestHeader(
            value = "X-Signature",
            required = false
        ) signature: String?,
        @RequestBody rawBody: String
    ): Map<String, String> {

        webhookFacade.receive(
            eventId = eventId,
            signature = signature,
            rawBody = rawBody
        )

        return mapOf("result" to "received")
    }
}