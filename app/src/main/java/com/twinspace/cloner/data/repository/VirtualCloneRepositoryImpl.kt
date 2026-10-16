package com.twinspace.cloner.data.repository

import com.twinspace.cloner.core.AppItem
import com.twinspace.cloner.core.CloneSettings
import com.twinspace.cloner.core.DualSpaceManager
import com.twinspace.cloner.domain.repository.VirtualCloneRepository

class VirtualCloneRepositoryImpl(
    private val dualSpaceManager: DualSpaceManager
) : VirtualCloneRepository {

    override fun getClonedApps(): List<AppItem> = dualSpaceManager.getClonedApps()

    override fun getInstalledApps(): List<AppItem> = dualSpaceManager.getInstalledApps()

    override fun cloneApp(packageName: String, settings: CloneSettings?): Boolean {
        return dualSpaceManager.cloneApp(packageName, settings)
    }

    override fun removeClone(app: AppItem): Boolean {
        return dualSpaceManager.removeClone(app)
    }

    override fun removeAllClones() {
        dualSpaceManager.removeAllClones()
    }

    override fun launchApp(app: AppItem) {
        dualSpaceManager.launchApp(app)
    }

    override fun isTwinProfileActive(): Boolean {
        return dualSpaceManager.isTwinProfileActive()
    }
}
