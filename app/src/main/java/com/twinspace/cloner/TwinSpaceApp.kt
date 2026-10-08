package com.twinspace.cloner

import android.app.Application
import com.twinspace.cloner.core.DualSpaceManager
import com.twinspace.cloner.core.PreferencesManager
import com.twinspace.cloner.core.SecurityManager

class TwinSpaceApp : Application() {
    lateinit var prefs: PreferencesManager
        private set

    lateinit var securityManager: SecurityManager
        private set

    lateinit var dualSpaceManager: DualSpaceManager
        private set

    lateinit var standaloneCloneRepository: com.twinspace.cloner.domain.repository.StandaloneCloneRepository
        private set

    lateinit var virtualCloneRepository: com.twinspace.cloner.domain.repository.VirtualCloneRepository
        private set

    lateinit var shortcutRepository: com.twinspace.cloner.domain.repository.ShortcutRepository
        private set

    lateinit var securityRepository: com.twinspace.cloner.domain.repository.SecurityRepository
        private set

    lateinit var storageOptimizerRepository: com.twinspace.cloner.domain.repository.StorageOptimizerRepository
        private set

    override fun onCreate() {
        super.onCreate()
        prefs = PreferencesManager(this)
        securityManager = SecurityManager(prefs)
        dualSpaceManager = DualSpaceManager(this, prefs)
        val standaloneCloner = com.twinspace.cloner.core.StandaloneApkCloner(this)
        standaloneCloneRepository = com.twinspace.cloner.data.repository.StandaloneCloneRepositoryImpl(standaloneCloner)
        virtualCloneRepository = com.twinspace.cloner.data.repository.VirtualCloneRepositoryImpl(dualSpaceManager)
        shortcutRepository = com.twinspace.cloner.data.repository.ShortcutRepositoryImpl(this)
        securityRepository = com.twinspace.cloner.data.repository.SecurityRepositoryImpl(this)
        storageOptimizerRepository = com.twinspace.cloner.data.repository.StorageOptimizerRepositoryImpl(this)
    }

    companion object {
        lateinit var instance: TwinSpaceApp
            private set
    }

    init {
        instance = this
    }
}
