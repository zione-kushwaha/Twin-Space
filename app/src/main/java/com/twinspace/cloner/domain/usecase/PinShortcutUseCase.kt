package com.twinspace.cloner.domain.usecase

import com.twinspace.cloner.core.AppItem
import com.twinspace.cloner.domain.repository.ShortcutRepository

/**
 * Use case to pin a direct launcher shortcut to the Android home screen.
 */
class PinShortcutUseCase(
    private val shortcutRepository: ShortcutRepository
) {
    operator fun invoke(app: AppItem, customName: String? = null, cloneIndex: Int = 1): Boolean {
        if (!shortcutRepository.isShortcutPinningSupported()) {
            return false
        }
        return shortcutRepository.pinAppShortcut(app, customName, cloneIndex)
    }

    fun isSupported(): Boolean = shortcutRepository.isShortcutPinningSupported()
}
