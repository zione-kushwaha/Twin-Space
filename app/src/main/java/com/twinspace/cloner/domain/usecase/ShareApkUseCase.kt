package com.twinspace.cloner.domain.usecase

import com.twinspace.cloner.domain.repository.StandaloneCloneRepository
import java.io.File

class ShareApkUseCase(
    private val repository: StandaloneCloneRepository
) {
    operator fun invoke(apkFile: File, appName: String): Result<Unit> {
        if (!apkFile.exists() || apkFile.length() <= 0L) {
            return Result.failure(IllegalArgumentException("APK file does not exist or is empty"))
        }
        return repository.shareApk(apkFile, appName)
    }
}
