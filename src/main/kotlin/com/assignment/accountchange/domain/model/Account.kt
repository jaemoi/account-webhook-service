package com.assignment.accountchange.domain.model

data class Account(
    val accountKey: String,
    val email: String?,
    val status: AccountStatus
)
