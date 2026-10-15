package com.twinspace.cloner.domain.repository

import com.twinspace.cloner.core.AppItem
import com.twinspace.cloner.core.CloneSettings

/**
 * Domain repository contract for virtual sandbox cloning inside DualSpace / Work Profile.
 */
interface VirtualCloneRepository {
    fun getClonedApps(): List<AppItem>
    fun getInstalledApps(): List<AppItem>
    fun cloneApp(packageName: String, settings: CloneSettings? = null): Boolean
    fun removeClone(app: AppItem): Boolean
    fun removeAllClones()
    fun launchApp(app: AppItem)
    fun isTwinProfileActive(): Boolean
}
