package com.assignment.accountchange.infra.persistence.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class SocialAccountRepository(
    private val jdbcTemplate: JdbcTemplate,
    private val accountRepository: AccountRepository
) {
    fun markProviderDeleted(accountKey: String, provider: String) {
        val accountId = accountRepository.getOrCreateAccountId(accountKey)
        val now = LocalDateTime.now().toString()

        val sql = """
            INSERT INTO social_accounts (account_id, provider, status, linked_at, updated_at)
            VALUES (?, ?, 'DELETED', ?, ?)
            ON CONFLICT(account_id, provider) DO UPDATE SET
              status = 'DELETED',
              updated_at = excluded.updated_at
        """.trimIndent()

        jdbcTemplate.update(sql, accountId, provider, now, now)
    }
}