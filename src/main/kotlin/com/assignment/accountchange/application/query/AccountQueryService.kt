package com.assignment.accountchange.application.query

import com.assignment.accountchange.application.query.dto.AccountQueryResult
import com.assignment.accountchange.application.query.dto.SocialAccountResult
import com.assignment.accountchange.infra.persistence.repository.AccountRepository
import com.assignment.accountchange.infra.persistence.repository.SocialAccountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AccountQueryService(
    private val accountRepository: AccountRepository,
    private val socialRepository: SocialAccountRepository
) {

    fun getAccount(accountKey: String): AccountQueryResult {
        val account = accountRepository.findByKey(accountKey)
            ?: throw IllegalArgumentException("Account not found")

        val socials = socialRepository.findByAccountKey(accountKey)

        return AccountQueryResult(
            accountKey = account.accountKey,
            email = account.email,
            status = account.status,
            socialAccounts = socials.map {
                SocialAccountResult(it.provider, it.status)
            }
        )
    }
}