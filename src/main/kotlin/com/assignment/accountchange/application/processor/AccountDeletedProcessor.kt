package com.assignment.accountchange.application.processor

import com.assignment.accountchange.domain.model.EventType
import com.assignment.accountchange.domain.model.InboxEvent
import com.assignment.accountchange.infra.persistence.repository.AccountRepository
import org.springframework.stereotype.Component

@Component
class AccountDeletedProcessor(
    private val accountRepository: AccountRepository
) : EventProcessor {

    override val supportedType =
        EventType.ACCOUNT_DELETED

    override fun process(event: InboxEvent) {
        accountRepository.markDeleted(event.accountKey)
    }
}