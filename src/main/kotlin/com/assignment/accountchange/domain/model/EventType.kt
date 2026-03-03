package com.assignment.accountchange.domain.model

enum class EventType {
    EMAIL_FORWARDING_CHANGED,
    ACCOUNT_DELETED,
    SOCIAL_ACCOUNT_DELETED,
    UNKNOWN;

    companion object {

        fun from(value: String): EventType =
            entries.firstOrNull {
                it.name.equals(value.trim(), ignoreCase = true)
            }
                ?: UNKNOWN
    }
}