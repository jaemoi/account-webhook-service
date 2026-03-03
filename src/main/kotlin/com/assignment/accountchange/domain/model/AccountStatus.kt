package com.assignment.accountchange.domain.model

enum class AccountStatus {
    ACTIVE,
    DELETED;

    companion object {
        fun from(value: String): AccountStatus =
            entries.firstOrNull {
                it.name.equals(value.trim(), true)
            }
                ?: throw IllegalArgumentException("Unknown status: $value")
    }
}