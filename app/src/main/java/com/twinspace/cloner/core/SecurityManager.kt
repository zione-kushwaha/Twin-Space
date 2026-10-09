package com.twinspace.cloner.core

class SecurityManager(private val prefs: PreferencesManager) {
    private var isSessionUnlocked: Boolean = false

    fun isLocked(): Boolean {
        if (!prefs.isDisguiseModeEnabled && !prefs.isAppLockEnabled) {
            return false
        }
        return !isSessionUnlocked
    }

    fun verifyPin(input: String): Boolean {
        val matches = input.trim() == prefs.secretPin.trim()
        if (matches) {
            isSessionUnlocked = true
        }
        return matches
    }

    fun lockSession() {
        isSessionUnlocked = false
    }

    fun unlockSession() {
        isSessionUnlocked = true
    }
}
