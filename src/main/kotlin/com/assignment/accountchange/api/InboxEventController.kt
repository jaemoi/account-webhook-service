package com.assignment.accountchange.api

import com.assignment.accountchange.application.query.InboxEventQueryService
import com.assignment.accountchange.application.service.InboxEventProcessorService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/inbox")
class InboxEventController(
    private val queryService: InboxEventQueryService,
    private val service: InboxEventProcessorService
) {

    @GetMapping("/events/{eventId}")
    fun getEvent(
        @PathVariable eventId: String
    ): Map<String, Any?> {

        val event = queryService.getEvent(eventId)

        return mapOf(
            "eventId" to event.eventId,
            "eventType" to event.eventType,
            "accountKey" to event.accountKey,
            "status" to event.status,
            "errorMessage" to event.errorMessage,
            "receivedAt" to event.receivedAt,
            "processedAt" to event.processedAt
        )
    }

    @PostMapping("/process")
    fun processOnce(): Map<String, Any?> {
        return when (val result = service.processOne()) {
            is InboxEventProcessorService.ProcessResult.NothingToProcess ->
                mapOf("result" to "nothing_to_process")

            is InboxEventProcessorService.ProcessResult.Processed ->
                mapOf("result" to "processed", "eventId" to result.eventId)

            is InboxEventProcessorService.ProcessResult.Failed ->
                mapOf(
                    "result" to "failed",
                    "eventId" to result.eventId,
                    "reason" to result.reason
                )
        }
    }
}