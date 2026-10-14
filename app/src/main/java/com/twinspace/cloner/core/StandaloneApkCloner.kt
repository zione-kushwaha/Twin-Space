package com.twinspace.cloner.core

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import com.android.apksig.ApkSigner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * High-performance standalone APK Cloner and Signer.
 * Modifies package names, strips old signatures, re-signs with v1+v2+v3 signatures,
 * and launches native Android PackageInstaller so the clone is installed as a true,
 * separate standalone app in the phone's system app drawer.
 */
class StandaloneApkCloner(private val context: Context) {

    companion object {
        private const val TAG = "StandaloneApkCloner"
        private const val KEYSTORE_ASSET = "signer.jks"
        private const val KEYSTORE_PASS = "twinspace2026"
        private const val KEY_ALIAS = "twinspace"
    }

    sealed class CloneStatus {
        data class Progress(val percent: Float, val message: String) : CloneStatus()
        data class Success(val apkFile: File, val targetPackage: String, val appName: String) : CloneStatus()
        data class Error(val error: String) : CloneStatus()
    }

    /**
     * Builds a standalone cloned APK for [originalPackage], signed and ready for installation.
     */
    suspend fun cloneAndBuildApk(
        originalPackage: String,
        cloneNumber: Int,
        onStatus: (CloneStatus) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        try {
            onStatus(CloneStatus.Progress(0.1f, "Extracting original APK..."))
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(originalPackage, 0)
            val appName = pm.getApplicationLabel(appInfo).toString()
            val sourceApk = File(appInfo.sourceDir)

            if (!sourceApk.exists()) {
                onStatus(CloneStatus.Error("Source APK not found at ${sourceApk.absolutePath}"))
                return@withContext null
            }

            val targetPackage = "$originalPackage.c$cloneNumber"
            val outputDir = File(context.getExternalFilesDir("clones"), originalPackage).apply { mkdirs() }
            val unsignedApk = File(outputDir, "temp_unsigned.apk")
            val sanitizedName = appName.replace("[^a-zA-Z0-9]+".toRegex(), "_").trim('_').ifBlank { "ClonedApp" }
            val signedApk = File(outputDir, "${sanitizedName}_Clone$cloneNumber.apk")

            onStatus(CloneStatus.Progress(0.3f, "Modifying manifest for package $targetPackage..."))

            // Repackage APK with modified AndroidManifest.xml
            ZipFile(sourceApk).use { zipIn ->
                ZipOutputStream(FileOutputStream(unsignedApk).buffered()).use { zipOut ->
                    val entries = zipIn.entries()
                    while (entries.hasMoreElements()) {
                        val entry = entries.nextElement()
                        val name = entry.name

                        // Skip existing signature files
                        if (name.startsWith("META-INF/") && (
                            name.endsWith(".SF") || name.endsWith(".RSA") ||
                            name.endsWith(".DSA") || name.endsWith(".EC") ||
                            name == "META-INF/MANIFEST.MF"
                        )) {
                            continue
                        }

                        if (name == "AndroidManifest.xml") {
                            val origManifestBytes = zipIn.getInputStream(entry).readBytes()
                            val modifiedManifest = AxmlEditor.modifyManifest(
                                manifestBytes = origManifestBytes,
                                originalPackage = originalPackage,
                                targetPackage = targetPackage
                            )
                            val manifestEntry = ZipEntry("AndroidManifest.xml")
                            zipOut.putNextEntry(manifestEntry)
                            zipOut.write(modifiedManifest)
                            zipOut.closeEntry()
                        } else {
                            val newEntry = ZipEntry(name)
                            if (entry.method == ZipEntry.STORED) {
                                newEntry.method = ZipEntry.STORED
                                newEntry.size = entry.size
                                newEntry.compressedSize = entry.compressedSize
                                newEntry.crc = entry.crc
                            }
                            zipOut.putNextEntry(newEntry)
                            zipIn.getInputStream(entry).copyTo(zipOut)
                            zipOut.closeEntry()
                        }
                    }
                }
            }

            onStatus(CloneStatus.Progress(0.7f, "Signing APK with official v1+v2+v3 signature..."))

            // Load signing key and certificate from assets
            val keyStore = KeyStore.getInstance("JKS")
            context.assets.open(KEYSTORE_ASSET).use { stream ->
                keyStore.load(stream, KEYSTORE_PASS.toCharArray())
            }
            val privateKey = keyStore.getKey(KEY_ALIAS, KEYSTORE_PASS.toCharArray()) as PrivateKey
            val certificate = keyStore.getCertificate(KEY_ALIAS) as X509Certificate

            val signerConfig = ApkSigner.SignerConfig.Builder(
                KEY_ALIAS,
                privateKey,
                listOf(certificate)
            ).build()

            if (signedApk.exists()) signedApk.delete()

            ApkSigner.Builder(listOf(signerConfig))
                .setInputApk(unsignedApk)
                .setOutputApk(signedApk)
                .setMinSdkVersion(26)
                .setV1SigningEnabled(true)
                .setV2SigningEnabled(true)
                .setV3SigningEnabled(true)
                .build()
                .sign()

            // Clean up temporary unsigned file
            unsignedApk.delete()

            Log.d(TAG, "Successfully built and signed standalone clone: ${signedApk.absolutePath}")
            onStatus(CloneStatus.Success(signedApk, targetPackage, appName))
            signedApk
        } catch (e: Exception) {
            Log.e(TAG, "Clone build failed: ${e.message}", e)
            onStatus(CloneStatus.Error(e.message ?: "Failed to build cloned APK"))
            null
        }
    }

    /**
     * Checks if the app has permission to request package installation.
     */
    fun canInstallApks(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    /**
     * Returns the intent to request "Install Unknown Apps" permission.
     */
    fun getInstallPermissionIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }

    /**
     * Launches Android PackageInstaller to install the cloned APK into the system app list.
     */
    fun installApk(apkFile: File) {
        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(installIntent)
    }

    /**
     * Opens Android system share sheet to send/export the cloned APK to other devices or people.
     */
    fun shareApk(apkFile: File, appName: String) {
        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.android.package-archive"
            putExtra(Intent.EXTRA_STREAM, apkUri)
            putExtra(Intent.EXTRA_SUBJECT, "$appName (Cloned APK)")
            putExtra(Intent.EXTRA_TEXT, "Here is the standalone cloned APK for $appName.")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Share Cloned APK with other people").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    /**
     * Returns a list of generated cloned APKs available in storage.
     */
    fun getSavedClonedApks(): List<File> {
        val dir = context.getExternalFilesDir("clones") ?: return emptyList()
        val apks = mutableListOf<File>()
        dir.walkTopDown().forEach { file ->
            if (file.isFile && file.extension.lowercase() == "apk") {
                apks.add(file)
            }
        }
        return apks.sortedByDescending { it.lastModified() }
    }
}
