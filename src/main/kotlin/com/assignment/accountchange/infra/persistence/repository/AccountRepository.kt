package com.assignment.accountchange.infra.persistence.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class AccountRepository(
    private val jdbcTemplate: JdbcTemplate
) {
    fun upsertEmail(accountKey: String, email: String?) {
        val now = LocalDateTime.now().toString()
        val sql = """
            INSERT INTO accounts (account_key, email, service_status, created_at, updated_at)
            VALUES (?, ?, 'ACTIVE', ?, ?)
            ON CONFLICT(account_key) DO UPDATE SET
              email = excluded.email,
              updated_at = excluded.updated_at
        """.trimIndent()

        jdbcTemplate.update(sql, accountKey, email, now, now)
    }

    fun markDeleted(accountKey: String) {
        val now = LocalDateTime.now().toString()
        val sql = """
            INSERT INTO accounts (account_key, email, service_status, created_at, updated_at)
            VALUES (?, NULL, 'DELETED', ?, ?)
            ON CONFLICT(account_key) DO UPDATE SET
              service_status = 'DELETED',
              updated_at = excluded.updated_at
        """.trimIndent()

        jdbcTemplate.update(sql, accountKey, now, now)
    }

    fun getOrCreateAccountId(accountKey: String): Long {
        val selectSql = "SELECT id FROM accounts WHERE account_key = ?"
        val existing = jdbcTemplate.query(
            selectSql,
            { rs, _ -> rs.getLong("id") },
            accountKey
        ).firstOrNull()
        if (existing != null) return existing

        val now = LocalDateTime.now().toString()
        val insertSql = """
            INSERT INTO accounts (account_key, email, service_status, created_at, updated_at)
            VALUES (?, NULL, 'ACTIVE', ?, ?)
        """.trimIndent()
        jdbcTemplate.update(insertSql, accountKey, now, now)

        // 다시 조회
        return jdbcTemplate.query(
            selectSql,
            { rs, _ -> rs.getLong("id") },
            accountKey
        ).first()
    }
}