package com.assignment.accountchange.infra.persistence.mapper

import com.assignment.accountchange.domain.model.InboxEvent
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import java.sql.ResultSet

@Component
class InboxEventRowMapper : RowMapper<InboxEvent> {
    override fun mapRow(rs: ResultSet, rowNum: Int) =
        rs.toInboxEvent()
}