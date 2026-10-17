package com.twinspace.cloner.domain.repository

import com.twinspace.cloner.core.AppItem

/**
 * Domain repository contract for pinning home screen shortcuts for cloned applications.
 */
interface ShortcutRepository {
    fun isShortcutPinningSupported(): Boolean
    fun pinAppShortcut(app: AppItem, customName: String? = null, cloneIndex: Int = 1): Boolean
}
