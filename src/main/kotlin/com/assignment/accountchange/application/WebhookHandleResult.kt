package com.assignment.accountchange.application

sealed interface WebhookHandleResult {
    data object Accepted : WebhookHandleResult
    data object AlreadyProcessed : WebhookHandleResult
    data object InProgress : WebhookHandleResult
}