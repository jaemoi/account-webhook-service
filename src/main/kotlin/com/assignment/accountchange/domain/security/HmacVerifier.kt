package com.assignment.accountchange.domain.security

interface HmacVerifier {
    fun verify(rawBody: String, signature: String): Boolean
}