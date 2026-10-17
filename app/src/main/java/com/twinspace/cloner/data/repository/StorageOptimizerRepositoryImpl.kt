package com.twinspace.cloner.data.repository

import android.content.Context
import com.twinspace.cloner.domain.model.StorageUsageSummary
import com.twinspace.cloner.domain.repository.StorageOptimizerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class StorageOptimizerRepositoryImpl(
    private val context: Context
) : StorageOptimizerRepository {

    override suspend fun calculateStorageUsage(): StorageUsageSummary = withContext(Dispatchers.IO) {
        val apkDir = File(context.getExternalFilesDir(null), "cloned_apks")
        val apkSize = calculateDirectorySize(apkDir)

        val cacheSize = calculateDirectorySize(context.cacheDir) +
                calculateDirectorySize(context.externalCacheDir)

        val tempDir = File(context.filesDir, "temp_build")
        val tempSize = calculateDirectorySize(tempDir)

        StorageUsageSummary(
            apkFilesSizeBytes = apkSize,
            cacheSizeBytes = cacheSize,
            tempFilesSizeBytes = tempSize
        )
    }

    override suspend fun cleanTemporaryCache(): Long = withContext(Dispatchers.IO) {
        var reclaimed = 0L

        // Clean cache directories
        reclaimed += deleteContents(context.cacheDir)
        reclaimed += deleteContents(context.externalCacheDir)

        // Clean temp build directory
        val tempDir = File(context.filesDir, "temp_build")
        reclaimed += deleteContents(tempDir)

        reclaimed
    }

    private fun calculateDirectorySize(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var total = 0L
        dir.walkTopDown().forEach { file ->
            if (file.isFile) {
                total += file.length()
            }
        }
        return total
    }

    private fun deleteContents(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var deletedBytes = 0L
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                deletedBytes += deleteContents(file)
                file.delete()
            } else {
                val len = file.length()
                if (file.delete()) {
                    deletedBytes += len
                }
            }
        }
        return deletedBytes
    }
}
