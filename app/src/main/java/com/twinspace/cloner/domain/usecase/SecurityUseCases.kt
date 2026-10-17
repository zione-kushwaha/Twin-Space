package com.twinspace.cloner.domain.usecase

import com.twinspace.cloner.domain.model.SecuritySettings
import com.twinspace.cloner.domain.repository.SecurityRepository

class GetSecuritySettingsUseCase(
    private val securityRepository: SecurityRepository
) {
    operator fun invoke(): SecuritySettings = securityRepository.getSecuritySettings()
}

class UpdateSecuritySettingsUseCase(
    private val securityRepository: SecurityRepository
) {
    operator fun invoke(settings: SecuritySettings) {
        securityRepository.updateSecuritySettings(settings)
    }
}

class VerifyPinUseCase(
    private val securityRepository: SecurityRepository
) {
    operator fun invoke(pin: String): Boolean = securityRepository.verifyPin(pin)
}

class CheckAppLockStateUseCase(
    private val securityRepository: SecurityRepository
) {
    /**
     * Returns true if lock is enabled AND app is currently locked.
     */
    operator fun invoke(): Boolean {
        val settings = securityRepository.getSecuritySettings()
        if (!settings.isLockEnabled || settings.masterPin.isEmpty()) {
            return false
        }
        return !securityRepository.isAppUnlocked()
    }
}

class UnlockAppUseCase(
    private val securityRepository: SecurityRepository
) {
    operator fun invoke() {
        securityRepository.setAppUnlocked(true)
    }
}

class LockAppUseCase(
    private val securityRepository: SecurityRepository
) {
    operator fun invoke() {
        securityRepository.setAppUnlocked(false)
    }
}
