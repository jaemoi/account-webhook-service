package com.assignment.accountchange.infra.security

import com.assignment.accountchange.domain.security.HmacVerifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Component
class HmacSha256Verifier(
    @field:Value("\${webhook.secret}")
    private val secret: String
) : HmacVerifier {

    override fun verify(rawBody: String, signature: String): Boolean {

        val normalized = signature
            .removePrefix("sha256=")
            .trim()

        val expected = generateHmac(rawBody)

        return MessageDigest.isEqual(
            hexToBytes(expected),
            hexToBytes(normalized)
        )
    }

    private fun generateHmac(data: String): String {
        val mac = Mac.getInstance("HmacSHA256")

        val key = SecretKeySpec(
            secret.toByteArray(Charsets.UTF_8),
            "HmacSHA256"
        )

        mac.init(key)

        val rawHmac = mac.doFinal(data.toByteArray(Charsets.UTF_8))

        return rawHmac.joinToString("") {
            "%02x".format(it)
        }
    }

    private fun hexToBytes(hex: String): ByteArray =
        hex.chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
}