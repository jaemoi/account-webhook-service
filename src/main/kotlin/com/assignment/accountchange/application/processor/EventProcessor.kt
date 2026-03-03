package com.assignment.accountchange.application.processor

import com.assignment.accountchange.domain.model.EventType
import com.assignment.accountchange.domain.model.InboxEvent

interface EventProcessor {

    val supportedType: EventType

    /** 이벤트를 실제로 처리한다.*/
    fun process(event: InboxEvent)
}