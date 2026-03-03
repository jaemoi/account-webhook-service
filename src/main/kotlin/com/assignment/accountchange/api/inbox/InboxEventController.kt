package com.assignment.accountchange.api.inbox

import com.assignment.accountchange.api.common.ApiResponse
import com.assignment.accountchange.api.inbox.response.InboxEventResponse
import com.assignment.accountchange.api.inbox.response.ProcessResultResponse
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
    ): ApiResponse<InboxEventResponse> {

        val event = queryService.getEvent(eventId)

        val response = InboxEventResponse(
            eventId = event.eventId,
            eventType = event.eventType.name,
            accountKey = event.accountKey,
            status = event.status.name,
            errorMessage = event.errorMessage,
            receivedAt = event.receivedAt,
            processedAt = event.processedAt
        )
        return ApiResponse.ok(response)
    }

    @PostMapping("/process")
    fun processOnce(): ApiResponse<ProcessResultResponse> {
        val response = when (val result = service.processOne()) {
            is InboxEventProcessorService.ProcessResult.NothingToProcess ->
                ProcessResultResponse(
                    result = "nothing_to_process"
                )

            is InboxEventProcessorService.ProcessResult.Processed ->
                ProcessResultResponse(
                    result = "processed",
                    eventId = result.eventId
                )

            is InboxEventProcessorService.ProcessResult.Failed ->
                ProcessResultResponse(
                    result = "failed",
                    eventId = result.eventId,
                    reason = result.reason
                )
        }
        return ApiResponse.ok(response)
    }
}