package com.twinspace.cloner.domain.repository

import com.twinspace.cloner.domain.model.CloneTaskStatus
import com.twinspace.cloner.domain.model.ClonedApk
import java.io.File
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository contract for building, discovering, installing, and sharing standalone APK clones.
 */
interface StandaloneCloneRepository {

    suspend fun getSavedClonedApks(): List<ClonedApk>

    suspend fun buildStandaloneApk(
        originalPackage: String,
        cloneNumber: Int,
        onStatus: (CloneTaskStatus) -> Unit
    ): Result<File>

    fun installApk(apkFile: File): Result<Unit>

    fun shareApk(apkFile: File, appName: String): Result<Unit>

    fun deleteApk(apkFile: File): Boolean

    fun canInstallApks(): Boolean
}
