package com.assignment.accountchange.api

import com.assignment.accountchange.application.service.InboxEventProcessorService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/inbox")
class InboxEventController(
    private val service: InboxEventProcessorService
) {

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