package com.twinspace.cloner.domain.model

import java.util.Locale

/**
 * Domain entity representing storage footprints of cloned APKs and caches.
 */
data class StorageUsageSummary(
    val apkFilesSizeBytes: Long = 0L,
    val cacheSizeBytes: Long = 0L,
    val tempFilesSizeBytes: Long = 0L
) {
    val totalSizeBytes: Long get() = apkFilesSizeBytes + cacheSizeBytes + tempFilesSizeBytes
    val formattedApkSize: String get() = formatBytes(apkFilesSizeBytes)
    val formattedCacheSize: String get() = formatBytes(cacheSizeBytes)
    val formattedTempSize: String get() = formatBytes(tempFilesSizeBytes)
    val formattedTotalSize: String get() = formatBytes(totalSizeBytes)

    companion object {
        fun formatBytes(bytes: Long): String {
            if (bytes <= 0L) return "0.0 MB"
            val kb = bytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
                mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
                kb >= 1.0 -> String.format(Locale.US, "%.1f KB", kb)
                else -> "$bytes B"
            }
        }
    }
}
