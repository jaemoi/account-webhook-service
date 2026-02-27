package com.assignment.accountchange.infra.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
data class InboxEventEntity(
    @Id
    var id: Long? = null
)
