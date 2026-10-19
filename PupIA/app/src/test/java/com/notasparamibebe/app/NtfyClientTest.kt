package com.notasparamibebe.app

import com.notasparamibebe.app.network.NtfyClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NtfyClientTest {

    @Test
    fun testSanitizeTopic_normalCode() {
        val topic = NtfyClient.sanitizeTopic("nuestro-amor")
        assertEquals("notas_bebe_nuestro-amor", topic)
    }

    @Test
    fun testSanitizeTopic_uppercaseAndSpaces() {
        val topic = NtfyClient.sanitizeTopic("  Mi Bebe Hermoso 123  ")
        assertEquals("notas_bebe_mi_bebe_hermoso_123", topic)
    }

    @Test
    fun testSanitizeTopic_specialCharacters() {
        val topic = NtfyClient.sanitizeTopic("amor!@#$%^&*()_+{}")
        assertTrue(topic.startsWith("notas_bebe_"))
        // Check that all characters after prefix are [a-z0-9_-]
        val cleanSuffix = topic.removePrefix("notas_bebe_")
        assertTrue(cleanSuffix.matches(Regex("^[a-z0-9_-]+$")))
    }

    @Test
    fun testSanitizeTopic_blankReturnsDefault() {
        val topic = NtfyClient.sanitizeTopic("   ")
        assertEquals("notas_bebe_default", topic)
    }

    @Test
    fun testBuildAndParsePayload_standardMessage() {
        val senderId = "device-uuid-1234"
        val message = "Te amo mucho mi cielo"
        val payload = NtfyClient.buildPayload(senderId, message)

        val parsed = NtfyClient.parsePayload(payload)
        assertNotNull(parsed)
        assertEquals(senderId, parsed?.first)
        assertEquals(message, parsed?.second)
    }

    @Test
    fun testBuildAndParsePayload_withSinglePipeAndEmojis() {
        val senderId = "sender-5678"
        val message = "Nota con un pipe simple | y emojis ❤️👶💍 y saltos de línea\nSegunda línea\nTercera línea"
        val payload = NtfyClient.buildPayload(senderId, message)

        val parsed = NtfyClient.parsePayload(payload)
        assertNotNull(parsed)
        assertEquals(senderId, parsed?.first)
        assertEquals(message, parsed?.second)
    }

    @Test
    fun testParsePayload_invalidWithoutDelimiter() {
        val parsed = NtfyClient.parsePayload("Mensaje directo sin delimitador de remitente")
        assertNull(parsed)
    }
}
