package com.twinspace.cloner.domain.repository

import com.twinspace.cloner.domain.model.SecuritySettings

/**
 * Domain repository contract for App Lock, Master PIN, and Privacy Vault configurations.
 */
interface SecurityRepository {
    fun getSecuritySettings(): SecuritySettings
    fun updateSecuritySettings(settings: SecuritySettings)
    fun verifyPin(pin: String): Boolean
    fun isAppUnlocked(): Boolean
    fun setAppUnlocked(unlocked: Boolean)
}
