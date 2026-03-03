package com.assignment.accountchange.application.service

import com.assignment.accountchange.application.dispatcher.EventProcessorDispatcher
import com.assignment.accountchange.infra.persistence.repository.InboxEventRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class InboxEventProcessorService(
    private val inboxEventRepository: InboxEventRepository,
    private val dispatcher: EventProcessorDispatcher
) {

    /**
     * RECEIVED 1건 처리 (없으면 null)
     */
    @Transactional
    fun processOne(): ProcessResult {
        val event = inboxEventRepository.findNextReceived()
            ?: return ProcessResult.NothingToProcess

        // 경쟁 상황에서 선점 실패하면 그냥 아무것도 안 한 걸로 종료(또 호출하면 됨)
        val locked = inboxEventRepository.tryMarkProcessing(event.eventId)
        if (!locked) return ProcessResult.NothingToProcess

        return try {
            dispatcher.dispatch(event)
            inboxEventRepository.markDone(event.eventId)
            ProcessResult.Processed(event.eventId)
        } catch (e: Exception) {
            inboxEventRepository.markFailed(event.eventId, e.message)
            ProcessResult.Failed(event.eventId, e.message)
        }
    }

    sealed interface ProcessResult {
        data object NothingToProcess : ProcessResult
        data class Processed(val eventId: String) : ProcessResult
        data class Failed(val eventId: String, val reason: String?) :
            ProcessResult
    }
}