package com.twinspace.cloner

import com.android.apksig.ApkSigner
import com.android.apksig.ApkVerifier
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ApkSigningTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testApkSignerAndVerifierIntegration() {
        // Load keystore from root directory or app assets
        val keystoreFile = File("../twinspace-release.jks").let {
            if (it.exists()) it else File("src/main/assets/signer.jks")
        }
        assertTrue("Keystore file must exist for signing tests", keystoreFile.exists())

        val keyStore = KeyStore.getInstance("JKS")
        FileInputStream(keystoreFile).use { fis ->
            keyStore.load(fis, "twinspace2026".toCharArray())
        }
        val privateKey = keyStore.getKey("twinspace", "twinspace2026".toCharArray()) as PrivateKey
        val cert = keyStore.getCertificate("twinspace") as X509Certificate

        assertNotNull("Private key must be loaded", privateKey)
        assertNotNull("Certificate must be loaded", cert)

        val baseApk = File("build/outputs/apk/release/app-release.apk").let {
            if (it.exists()) it else File("build/outputs/apk/debug/app-debug.apk")
        }
        var realManifestBytes: ByteArray? = null
        java.util.zip.ZipFile(baseApk).use { zip ->
            val entry = zip.getEntry("AndroidManifest.xml")
            realManifestBytes = zip.getInputStream(entry).readBytes()
        }

        // Create an unsigned APK containing the real binary AXML manifest
        val unsignedApk = tempFolder.newFile("unsigned_test.apk")
        ZipOutputStream(FileOutputStream(unsignedApk)).use { zos ->
            zos.putNextEntry(ZipEntry("AndroidManifest.xml"))
            zos.write(realManifestBytes!!)
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("classes.dex"))
            zos.write("dex\n035\u0000dummy_dex_data".toByteArray())
            zos.closeEntry()
        }

        val signedApk = tempFolder.newFile("signed_test.apk")
        val signerConfig = ApkSigner.SignerConfig.Builder(
            "twinspace",
            privateKey,
            listOf(cert)
        ).build()

        // Sign with ApkSigner (v1, v2, v3)
        ApkSigner.Builder(listOf(signerConfig))
            .setInputApk(unsignedApk)
            .setOutputApk(signedApk)
            .setMinSdkVersion(26)
            .setV1SigningEnabled(true)
            .setV2SigningEnabled(true)
            .setV3SigningEnabled(true)
            .build()
            .sign()

        assertTrue("Signed APK must exist and have non-zero size", signedApk.exists() && signedApk.length() > 0)

        // Verify with Google's ApkVerifier
        val verifier = ApkVerifier.Builder(signedApk).build()
        val result = verifier.verify()

        assertTrue("Signed APK must be cryptographically verified", result.isVerified)
        assertTrue("APK must contain valid v2 or v3 signature scheme", result.isVerifiedUsingV2Scheme || result.isVerifiedUsingV3Scheme)
    }
}
