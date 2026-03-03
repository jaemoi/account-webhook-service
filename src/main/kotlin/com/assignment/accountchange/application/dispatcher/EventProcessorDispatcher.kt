package com.assignment.accountchange.application.dispatcher

import com.assignment.accountchange.application.processor.EventProcessor
import com.assignment.accountchange.domain.model.InboxEvent
import org.springframework.stereotype.Service

@Service
class EventProcessorDispatcher(
    processors: List<EventProcessor>
) {

    private val processorMap =
        processors.associateBy { it.supportedType }

    fun dispatch(event: InboxEvent) {

        val processor =
            processorMap[event.eventType]
                ?: error("Unsupported event: ${event.eventType}")

        processor.process(event)
    }
}