package com.assignment.accountchange.domain.model

enum class SocialAccountStatus {
    ACTIVE,
    DELETED;

    companion object {
        fun from(value: String): SocialAccountStatus {
            return entries.firstOrNull {
                it.name.equals(value.trim(), ignoreCase = true)
            }
                ?: throw IllegalArgumentException(
                    "Unknown SocialAccountStatus: $value"
                )
        }
    }
}

