package com.twinspace.cloner.domain.usecase

import com.twinspace.cloner.domain.repository.StandaloneCloneRepository
import java.io.File

class DeleteApkUseCase(
    private val repository: StandaloneCloneRepository
) {
    operator fun invoke(apkFile: File): Boolean {
        return repository.deleteApk(apkFile)
    }
}
