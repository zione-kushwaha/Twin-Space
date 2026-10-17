package com.twinspace.cloner.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.twinspace.cloner.domain.model.SecuritySettings
import com.twinspace.cloner.domain.repository.SecurityRepository

class SecurityRepositoryImpl(
    context: Context
) : SecurityRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("twinspace_security_prefs", Context.MODE_PRIVATE)

    @Volatile
    private var isSessionUnlocked: Boolean = false

    override fun getSecuritySettings(): SecuritySettings {
        return SecuritySettings(
            isLockEnabled = prefs.getBoolean(KEY_LOCK_ENABLED, false),
            masterPin = prefs.getString(KEY_MASTER_PIN, "") ?: "",
            isBiometricEnabled = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false),
            isCalculatorDisguiseEnabled = prefs.getBoolean(KEY_CALCULATOR_DISGUISE, false)
        )
    }

    override fun updateSecuritySettings(settings: SecuritySettings) {
        prefs.edit()
            .putBoolean(KEY_LOCK_ENABLED, settings.isLockEnabled)
            .putString(KEY_MASTER_PIN, settings.masterPin)
            .putBoolean(KEY_BIOMETRIC_ENABLED, settings.isBiometricEnabled)
            .putBoolean(KEY_CALCULATOR_DISGUISE, settings.isCalculatorDisguiseEnabled)
            .apply()
    }

    override fun verifyPin(pin: String): Boolean {
        val settings = getSecuritySettings()
        val isValid = settings.verifyPin(pin)
        if (isValid) {
            isSessionUnlocked = true
        }
        return isValid
    }

    override fun isAppUnlocked(): Boolean {
        val settings = getSecuritySettings()
        if (!settings.isLockEnabled || settings.masterPin.isEmpty()) {
            return true
        }
        return isSessionUnlocked
    }

    override fun setAppUnlocked(unlocked: Boolean) {
        isSessionUnlocked = unlocked
    }

    companion object {
        private const val KEY_LOCK_ENABLED = "key_lock_enabled"
        private const val KEY_MASTER_PIN = "key_master_pin"
        private const val KEY_BIOMETRIC_ENABLED = "key_biometric_enabled"
        private const val KEY_CALCULATOR_DISGUISE = "key_calculator_disguise"
    }
}
