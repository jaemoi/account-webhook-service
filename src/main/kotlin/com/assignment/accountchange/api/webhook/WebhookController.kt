package com.assignment.accountchange.api.webhook

import com.assignment.accountchange.api.common.ApiResponse
import com.assignment.accountchange.api.webhook.response.WebhookReceiveResponse
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
        @RequestHeader(value = "X-Signature", required = false)
        signature: String?,
        @RequestBody rawBody: String
    ): ApiResponse<WebhookReceiveResponse> {

        val result = webhookFacade.receive(
            eventId = eventId,
            signature = signature,
            rawBody = rawBody
        )

        val response = when (result) {
            WebhookHandleResult.Accepted ->
                WebhookReceiveResponse("received")

            WebhookHandleResult.AlreadyProcessed ->
                WebhookReceiveResponse("already_processed")

            WebhookHandleResult.InProgress ->
                WebhookReceiveResponse("processing")
        }
        return ApiResponse.ok(response)
    }
}