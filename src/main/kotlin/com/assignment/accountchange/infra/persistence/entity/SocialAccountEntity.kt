package com.assignment.accountchange.infra.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
data class SocialAccountEntity(
    @Id
    var id: Long? = null
)
