package com.twinspace.cloner.domain.model

/**
 * Domain entity representing security lock and privacy vault configurations.
 */
data class SecuritySettings(
    val isLockEnabled: Boolean = false,
    val masterPin: String = "",
    val isBiometricEnabled: Boolean = false,
    val isCalculatorDisguiseEnabled: Boolean = false
) {
    val hasPinConfigured: Boolean get() = masterPin.length >= 4

    fun verifyPin(inputPin: String): Boolean {
        if (!isLockEnabled || masterPin.isEmpty()) return true
        return inputPin == masterPin
    }
}
