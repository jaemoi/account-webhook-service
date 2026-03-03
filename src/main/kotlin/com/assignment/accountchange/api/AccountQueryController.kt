package com.assignment.accountchange.api

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
    ): Map<String, Any?> {

        val result = queryService.getAccount(accountKey)

        return mapOf(
            "accountKey" to result.accountKey,
            "email" to result.email,
            "status" to result.status,
            "socialAccounts" to result.socialAccounts
        )
    }
}