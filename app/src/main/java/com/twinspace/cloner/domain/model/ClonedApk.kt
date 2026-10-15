package com.twinspace.cloner.domain.model

import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Domain entity representing a standalone cloned APK file on disk.
 */
data class ClonedApk(
    val file: File,
    val fileName: String = file.name,
    val appName: String = parseAppName(file.name),
    val cloneNumber: Int = parseCloneNumber(file.name),
    val sizeBytes: Long = if (file.exists()) file.length() else 0L,
    val lastModifiedTimestamp: Long = if (file.exists()) file.lastModified() else 0L
) {
    val formattedSize: String
        get() = formatFileSize(sizeBytes)

    val formattedDate: String
        get() = formatTimestamp(lastModifiedTimestamp)

    companion object {
        fun parseAppName(fileName: String): String {
            val base = fileName.removeSuffix(".apk")
            val withoutClone = base.substringBefore("_Clone")
            return withoutClone.replace("_", " ").trim().ifBlank { "Cloned App" }
        }

        fun parseCloneNumber(fileName: String): Int {
            val base = fileName.removeSuffix(".apk")
            val clonePart = base.substringAfter("_Clone", "")
            return clonePart.toIntOrNull() ?: 1
        }

        fun formatFileSize(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
            val df = DecimalFormat("#,##0.#")
            return "${df.format(bytes / Math.pow(1024.0, digitGroups.toDouble()))} ${units[digitGroups]}"
        }

        fun formatTimestamp(timestamp: Long): String {
            if (timestamp <= 0L) return "Unknown"
            val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
}
