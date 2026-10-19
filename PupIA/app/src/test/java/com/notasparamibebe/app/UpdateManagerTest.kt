package com.notasparamibebe.app

import com.notasparamibebe.app.data.updater.UpdateManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateManagerTest {

    @Test
    fun testIsNewerVersion_patchUpgrade() {
        assertTrue(UpdateManager.isNewerVersion("1.0.0", "1.0.1"))
    }

    @Test
    fun testIsNewerVersion_withVPrefix() {
        assertTrue(UpdateManager.isNewerVersion("1.0.0", "v1.0.2"))
        assertTrue(UpdateManager.isNewerVersion("v1.0.0", "v1.1.0"))
    }

    @Test
    fun testIsNewerVersion_sameVersion_returnsFalse() {
        assertFalse(UpdateManager.isNewerVersion("1.0.0", "1.0.0"))
        assertFalse(UpdateManager.isNewerVersion("v1.0.1", "1.0.1"))
    }

    @Test
    fun testIsNewerVersion_olderCandidate_returnsFalse() {
        assertFalse(UpdateManager.isNewerVersion("1.2.0", "1.1.5"))
        assertFalse(UpdateManager.isNewerVersion("2.0.0", "1.9.9"))
    }
}
