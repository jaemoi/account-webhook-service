package com.assignment.accountchange.api

import com.assignment.accountchange.application.WebhookFacade
import com.assignment.accountchange.application.WebhookHandleResult
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

        val result = webhookFacade.receive(
            eventId = eventId,
            signature = signature,
            rawBody = rawBody
        )

        return when (result) {
            WebhookHandleResult.Accepted ->
                mapOf("result" to "received")

            WebhookHandleResult.AlreadyProcessed ->
                mapOf("result" to "already_processed")

            WebhookHandleResult.InProgress ->
                mapOf("result" to "processing")
        }
    }
}