package com.assignment.accountchange.application

sealed interface WebhookHandleResult {
    data object Accepted : WebhookHandleResult
    data object Duplicated : WebhookHandleResult
    data object InProgress : WebhookHandleResult
}