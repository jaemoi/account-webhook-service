package com.assignment.accountchange.infra.security

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class HmacSha256VerifierTest {

    private val verifier = HmacSha256Verifier("test-secret")

    @Test
    fun `valid signature should return true`() {

        val body = """{"eventType":"ACCOUNT_DELETED"}"""

        val signature = generateSignature("test-secret", body)

        val result = verifier.verify(body, signature)

        assertTrue(result)
    }

    @Test
    fun `invalid signature should return false`() {

        val body = """{"eventType":"ACCOUNT_DELETED"}"""

        val wrongSignature =
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"

        val result = verifier.verify(body, wrongSignature)

        assertFalse(result)
    }

    @Test
    fun `signature with sha256 prefix should pass`() {

        val body = """{"eventType":"ACCOUNT_DELETED"}"""

        val signature = generateSignature("test-secret", body)

        val result = verifier.verify(body, "sha256=$signature")

        assertTrue(result)
    }

    private fun generateSignature(secret: String, body: String): String {
        val mac = Mac.getInstance("HmacSHA256")

        val key = SecretKeySpec(
            secret.toByteArray(Charsets.UTF_8),
            "HmacSHA256"
        )

        mac.init(key)

        val raw = mac.doFinal(body.toByteArray(Charsets.UTF_8))

        return raw.joinToString("") { "%02x".format(it) }
    }
}