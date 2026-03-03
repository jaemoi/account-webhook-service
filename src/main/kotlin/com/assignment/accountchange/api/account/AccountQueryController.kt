package com.assignment.accountchange.api.account

import com.assignment.accountchange.api.account.response.AccountResponse
import com.assignment.accountchange.api.common.ApiResponse
import com.assignment.accountchange.application.query.AccountQueryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/accounts")
class AccountQueryController(
    private val queryService: AccountQueryService
) {

    @GetMapping("/{accountKey}")
    fun getAccount(
        @PathVariable accountKey: String
    ): ApiResponse<AccountResponse> {

        val result = queryService.getAccount(accountKey)

        val response = AccountResponse(
            accountKey = result.accountKey,
            email = result.email,
            status = result.status.name,
            socialAccounts = result.socialAccounts
        )

        return ApiResponse.ok(response)
    }
}