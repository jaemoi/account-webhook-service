package com.assignment.accountchange.infra.persistence.repository

import com.assignment.accountchange.domain.model.Provider
import com.assignment.accountchange.domain.model.SocialAccount
import com.assignment.accountchange.domain.model.SocialAccountStatus
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

    fun findByAccountKey(accountKey: String): List<SocialAccount> {

        val sql = """
        SELECT a.account_key, sa.provider, sa.status
        FROM social_accounts sa
        JOIN accounts a ON sa.account_id = a.id
        WHERE a.account_key = ?
    """.trimIndent()

        return jdbcTemplate.query(
            sql,
            { rs, _ ->
                SocialAccount(
                    accountKey = rs.getString("account_key"),
                    provider = Provider.valueOf(
                        rs.getString("provider")
                    ),
                    status = SocialAccountStatus.from(
                        rs.getString("status")
                    )
                )
            },
            accountKey
        )
    }
}