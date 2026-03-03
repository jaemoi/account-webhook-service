package com.assignment.accountchange.application.query.dto

import com.assignment.accountchange.domain.model.AccountStatus
import com.assignment.accountchange.domain.model.Provider
import com.assignment.accountchange.domain.model.SocialAccountStatus

data class AccountQueryResult(
    val accountKey: String,
    val email: String?,
    val status: AccountStatus,
    val socialAccounts: List<SocialAccountResult>
)

data class SocialAccountResult(
    val provider: Provider,
    val status: SocialAccountStatus
)
