package com.assignment.accountchange.api.account.response

import com.assignment.accountchange.application.query.dto.SocialAccountResult

data class AccountResponse(
    val accountKey: String,
    val email: String?,
    val status: String,
    val socialAccounts: List<SocialAccountResult>
)
