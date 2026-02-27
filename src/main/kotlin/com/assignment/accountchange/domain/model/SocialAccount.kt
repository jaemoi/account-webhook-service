package com.assignment.accountchange.domain.model

data class SocialAccount(
    val accountKey: String,
    val provider: Provider,
    val status: String
)
