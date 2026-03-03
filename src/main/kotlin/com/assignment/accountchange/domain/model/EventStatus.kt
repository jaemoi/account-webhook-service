package com.assignment.accountchange.domain.model

enum class EventStatus {
    RECEIVED,
    PROCESSING,
    DONE,
    FAILED;

    companion object {

        fun from(value: String): EventStatus =
            entries.firstOrNull {
                it.name.equals(value.trim(), ignoreCase = true)
            }
                ?: throw IllegalArgumentException("Unknown EventStatus: $value")
    }
}