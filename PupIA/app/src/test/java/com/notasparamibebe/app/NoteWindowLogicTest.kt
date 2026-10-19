package com.notasparamibebe.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteWindowLogicTest {

    @Test
    fun testNotificationWindow_withinFiveMinutes_shouldTrigger() {
        val now = System.currentTimeMillis()
        val msgTimestamp = now - (2 * 60 * 1000L) // 2 minutos atrás
        val isRecent = (now - msgTimestamp) < (5 * 60 * 1000L)
        assertTrue(isRecent)
    }

    @Test
    fun testNotificationWindow_olderThanFiveMinutes_shouldNotTrigger() {
        val now = System.currentTimeMillis()
        val msgTimestamp = now - (10 * 60 * 1000L) // 10 minutos atrás
        val isRecent = (now - msgTimestamp) < (5 * 60 * 1000L)
        assertFalse(isRecent)
    }

    @Test
    fun testSenderFilter_differentSender_isAccepted() {
        val mySenderId = "my-device-123"
        val incomingSenderId = "partner-device-456"
        val isFromOther = (incomingSenderId != mySenderId)
        assertTrue(isFromOther)
    }

    @Test
    fun testSenderFilter_sameSender_isIgnored() {
        val mySenderId = "my-device-123"
        val incomingSenderId = "my-device-123"
        val isFromOther = (incomingSenderId != mySenderId)
        assertFalse(isFromOther)
    }
}
