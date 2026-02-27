package com.assignment.accountchange.infra.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
data  class AccountEntity(
    @Id
    var id: Long? = null
)
