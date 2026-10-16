package com.twinspace.cloner.data.repository

import com.twinspace.cloner.core.StandaloneApkCloner
import com.twinspace.cloner.domain.model.CloneTaskStatus
import com.twinspace.cloner.domain.model.ClonedApk
import com.twinspace.cloner.domain.repository.StandaloneCloneRepository
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StandaloneCloneRepositoryImpl(
    private val standaloneCloner: StandaloneApkCloner
) : StandaloneCloneRepository {

    override suspend fun getSavedClonedApks(): List<ClonedApk> = withContext(Dispatchers.IO) {
        standaloneCloner.getSavedClonedApks().map { file ->
            ClonedApk(file = file)
        }
    }

    override suspend fun buildStandaloneApk(
        originalPackage: String,
        cloneNumber: Int,
        onStatus: (CloneTaskStatus) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        var resultFile: File? = null
        var caughtException: Exception? = null

        val apkFile = standaloneCloner.cloneAndBuildApk(
            originalPackage = originalPackage,
            cloneNumber = cloneNumber,
            onStatus = { status ->
                when (status) {
                    is StandaloneApkCloner.CloneStatus.Progress -> {
                        val step = when {
                            status.percent < 0.35f -> CloneTaskStatus.PipelineStep.EXTRACTING
                            status.percent < 0.70f -> CloneTaskStatus.PipelineStep.REWRITING
                            status.percent < 1.0f -> CloneTaskStatus.PipelineStep.SIGNING
                            else -> CloneTaskStatus.PipelineStep.COMPLETED
                        }
                        onStatus(CloneTaskStatus.InProgress(step, status.percent, status.message))
                    }
                    is StandaloneApkCloner.CloneStatus.Success -> {
                        resultFile = status.apkFile
                        onStatus(
                            CloneTaskStatus.Success(
                                apkFile = status.apkFile,
                                appName = ClonedApk.parseAppName(status.apkFile.name),
                                cloneNumber = cloneNumber
                            )
                        )
                    }
                    is StandaloneApkCloner.CloneStatus.Error -> {
                        caughtException = IllegalStateException(status.error)
                        onStatus(CloneTaskStatus.Error(status.error))
                    }
                }
            }
        )

        if (apkFile != null && apkFile.exists() && apkFile.length() > 0) {
            Result.success(apkFile)
        } else {
            Result.failure(caughtException ?: IllegalStateException("Failed to build standalone APK"))
        }
    }

    override fun installApk(apkFile: File): Result<Unit> {
        return try {
            standaloneCloner.installApk(apkFile)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun shareApk(apkFile: File, appName: String): Result<Unit> {
        return try {
            standaloneCloner.shareApk(apkFile, appName)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun deleteApk(apkFile: File): Boolean {
        return if (apkFile.exists()) {
            apkFile.delete()
        } else {
            false
        }
    }

    override fun canInstallApks(): Boolean {
        return standaloneCloner.canInstallApks()
    }
}
