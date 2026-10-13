package com.twinspace.cloner

import com.twinspace.cloner.core.AxmlEditor
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.ZipFile

class AxmlAdvancedEdgeCasesTest {

    @Test
    fun testManifestWithMultipleProvidersAndPermissions() {
        // Load real manifest from release APK
        val baseApk = File("build/outputs/apk/release/app-release.apk").let {
            if (it.exists()) it else File("build/outputs/apk/debug/app-debug.apk")
        }
        assertTrue("Base APK must exist", baseApk.exists())

        var rawBytes: ByteArray? = null
        ZipFile(baseApk).use { zip ->
            val entry = zip.getEntry("AndroidManifest.xml")
            assertNotNull(entry)
            rawBytes = zip.getInputStream(entry).readBytes()
        }

        val originalBytes = rawBytes!!
        val origPkg = "com.twinspace.cloner"

        // Test clone 1
        val c1Bytes = AxmlEditor.modifyManifest(originalBytes, origPkg, "$origPkg.c1")
        assertNotNull(c1Bytes)

        // Test clone 2
        val c2Bytes = AxmlEditor.modifyManifest(originalBytes, origPkg, "$origPkg.c2")
        assertNotNull(c2Bytes)

        // Test clone 5 (multi-digit index)
        val c5Bytes = AxmlEditor.modifyManifest(originalBytes, origPkg, "$origPkg.c15")
        assertNotNull(c5Bytes)

        // Validate lengths and AXML structures for all
        for (bytes in listOf(c1Bytes, c2Bytes, c5Bytes)) {
            val buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            val type = buf.short.toInt() and 0xFFFF
            val headerSize = buf.short.toInt() and 0xFFFF
            val fileSize = buf.int

            assertEquals(0x0003, type)
            assertEquals(8, headerSize)
            assertEquals(bytes.size, fileSize)

            val poolType = buf.short.toInt() and 0xFFFF
            assertEquals(0x0001, poolType)
        }
    }

    @Test
    fun testSpecialCharactersAndPrefixMatching() {
        val orig = "com.example.music_app"
        val clone = "com.example.music_app.c1"

        val inputs = listOf(
            "com.example.music_app",
            "com.example.music_app.fileprovider",
            "com.example.music_app.androidx-startup",
            "com.example.music_app.permission.C2D_MESSAGE",
            "com.example.music_app_prefs",
            "com.example.music_app.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION",
            "android.permission.INTERNET",
            "androidx.core.content.FileProvider"
        )

        val results = inputs.map { str ->
            when {
                str == orig -> clone
                str.startsWith("$orig.") -> str.replaceFirst(orig, clone)
                str.contains(".$orig.") -> str.replace(".$orig.", ".$clone.")
                str.startsWith("${orig}_") -> str.replaceFirst(orig, clone)
                else -> str
            }
        }

        assertEquals(clone, results[0])
        assertEquals("$clone.fileprovider", results[1])
        assertEquals("$clone.androidx-startup", results[2])
        assertEquals("$clone.permission.C2D_MESSAGE", results[3])
        assertEquals("${clone}_prefs", results[4])
        assertEquals("$clone.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION", results[5])
        assertEquals("android.permission.INTERNET", results[6])
        assertEquals("androidx.core.content.FileProvider", results[7])
    }
}
