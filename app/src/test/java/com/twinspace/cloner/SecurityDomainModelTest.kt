package com.twinspace.cloner

import com.twinspace.cloner.domain.model.SecuritySettings
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityDomainModelTest {

    @Test
    fun testPinVerificationWhenLockDisabled() {
        val settings = SecuritySettings(
            isLockEnabled = false,
            masterPin = "1234"
        )
        // When lock is disabled, any pin is accepted
        assertTrue(settings.verifyPin("9999"))
        assertTrue(settings.verifyPin("1234"))
    }

    @Test
    fun testPinVerificationWhenLockEnabled() {
        val settings = SecuritySettings(
            isLockEnabled = true,
            masterPin = "5678"
        )
        assertTrue(settings.verifyPin("5678"))
        assertFalse(settings.verifyPin("0000"))
        assertFalse(settings.verifyPin(""))
    }

    @Test
    fun testHasPinConfigured() {
        val settingsShort = SecuritySettings(masterPin = "12")
        assertFalse(settingsShort.hasPinConfigured)

        val settingsValid = SecuritySettings(masterPin = "1234")
        assertTrue(settingsValid.hasPinConfigured)
    }
}
