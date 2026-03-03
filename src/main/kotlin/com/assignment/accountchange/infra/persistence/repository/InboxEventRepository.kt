package com.assignment.accountchange.infra.persistence.repository

import com.assignment.accountchange.domain.model.EventStatus
import com.assignment.accountchange.domain.model.EventType
import com.assignment.accountchange.domain.model.InboxEvent
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class InboxEventRepository(
    private val jdbcTemplate: JdbcTemplate
) {

    fun insert(
        eventId: String,
        eventType: EventType,
        accountKey: String,
        payload: String
    ) {
        val sql = """
            INSERT INTO inbox_events (
                event_id,
                event_type,
                account_key,
                payload,
                status,
                received_at
            )
            VALUES (?, ?, ?, ?, 'RECEIVED', ?)
        """.trimIndent()

        jdbcTemplate.update(
            sql,
            eventId,
            eventType.name,
            accountKey,
            payload,
            LocalDateTime.now().toString()
        )
    }

    /**
     * 처리할 다음 RECEIVED 이벤트 1건 조회
     */
    fun findNextReceived(): InboxEvent? {
        val sql = """
            SELECT event_id, event_type, account_key, payload, status
            FROM inbox_events
            WHERE status = ?
            ORDER BY received_at ASC
            LIMIT 1
        """.trimIndent()

        return jdbcTemplate.query(sql, { rs, _ ->
            InboxEvent(
                eventId = rs.getString("event_id"),
                eventType = EventType.valueOf(rs.getString("event_type")),
                accountKey = rs.getString("account_key"),
                payload = rs.getString("payload"),
                status = EventStatus.valueOf(rs.getString("status"))
            )
        }, EventStatus.RECEIVED.name).firstOrNull()
    }

    /**
     * RECEIVED → PROCESSING (경쟁상황 대비: status 조건 걸어둠)
     * true면 내 프로세스가 선점 성공, false면 누가 먼저 가져감
     */
    fun tryMarkProcessing(eventId: String): Boolean {
        val sql = """
            UPDATE inbox_events
            SET status = ?, processed_at = ?
            WHERE event_id = ? AND status = ?
        """.trimIndent()

        val updated = jdbcTemplate.update(
            sql,
            EventStatus.PROCESSING.name,
            LocalDateTime.now().toString(),
            eventId,
            EventStatus.RECEIVED.name
        )
        return updated == 1
    }

    fun markDone(eventId: String) {
        val sql = """
            UPDATE inbox_events
            SET status = ?, processed_at = ?, error_message = NULL
            WHERE event_id = ?
        """.trimIndent()

        jdbcTemplate.update(
            sql,
            EventStatus.DONE.name,
            LocalDateTime.now().toString(),
            eventId
        )
    }

    fun markFailed(eventId: String, errorMessage: String?) {
        val sql = """
            UPDATE inbox_events
            SET status = ?, processed_at = ?, error_message = ?
            WHERE event_id = ?
        """.trimIndent()

        jdbcTemplate.update(
            sql,
            EventStatus.FAILED.name,
            LocalDateTime.now().toString(),
            errorMessage?.take(1000),
            eventId
        )
    }

    fun findStatusByEventId(eventId: String): EventStatus? {

        val sql = """
        SELECT status
        FROM inbox_events
        WHERE event_id = ?
    """.trimIndent()

        return jdbcTemplate.query(
            sql,
            { rs, _ ->
                EventStatus.valueOf(rs.getString("status"))
            },
            eventId
        ).firstOrNull()
    }
}