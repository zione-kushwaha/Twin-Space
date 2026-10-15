package com.twinspace.cloner.domain.usecase

import com.twinspace.cloner.domain.repository.StandaloneCloneRepository
import java.io.File

class InstallApkUseCase(
    private val repository: StandaloneCloneRepository
) {
    operator fun invoke(apkFile: File): Result<Unit> {
        if (!apkFile.exists() || apkFile.length() <= 0L) {
            return Result.failure(IllegalArgumentException("APK file does not exist or is empty"))
        }
        return repository.installApk(apkFile)
    }
}
