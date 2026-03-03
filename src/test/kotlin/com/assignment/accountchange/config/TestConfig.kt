package com.assignment.accountchange.config

import com.assignment.accountchange.domain.security.HmacVerifier
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class TestConfig {

    @Bean
    fun hmacVerifier(): HmacVerifier {
        return object : HmacVerifier {
            override fun verify(rawBody: String, signature: String): Boolean {
                return true
            }
        }
    }
}