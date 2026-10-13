package com.twinspace.cloner

import com.twinspace.cloner.core.AxmlEditor
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.ZipFile

class AxmlEditorTest {

    @Test
    fun testRealApkManifestModification() {
        // Find release or debug APK from build outputs
        val releaseApk = File("build/outputs/apk/release/app-release.apk")
        val debugApk = File("build/outputs/apk/debug/app-debug.apk")
        val targetApk = when {
            releaseApk.exists() -> releaseApk
            debugApk.exists() -> debugApk
            else -> null
        }

        assertNotNull("Build output APK must exist for integration testing", targetApk)

        var manifestBytes: ByteArray? = null
        ZipFile(targetApk).use { zip ->
            val entry = zip.getEntry("AndroidManifest.xml")
            assertNotNull("APK must contain AndroidManifest.xml", entry)
            manifestBytes = zip.getInputStream(entry).readBytes()
        }

        val originalBytes = manifestBytes!!
        assertTrue("AXML manifest size must be > 0", originalBytes.isNotEmpty())

        val originalPackage = "com.twinspace.cloner"
        val targetPackage = "com.twinspace.cloner.c1"

        // Perform modification
        val modifiedBytes = AxmlEditor.modifyManifest(
            manifestBytes = originalBytes,
            originalPackage = originalPackage,
            targetPackage = targetPackage
        )

        assertNotNull("Modified bytes must not be null", modifiedBytes)
        assertTrue("Modified AXML must have valid size", modifiedBytes.size > 0)

        // Verify AXML Header
        val buffer = ByteBuffer.wrap(modifiedBytes).order(ByteOrder.LITTLE_ENDIAN)
        val xmlType = buffer.short.toInt() and 0xFFFF
        val xmlHeaderSize = buffer.short.toInt() and 0xFFFF
        val totalFileSize = buffer.int

        assertEquals("RES_XML_TYPE must be 0x0003", 0x0003, xmlType)
        assertEquals("Header size must be 8", 8, xmlHeaderSize)
        assertEquals("File size in header must match actual byte array size", modifiedBytes.size, totalFileSize)

        // Verify String Pool Chunk
        val poolType = buffer.short.toInt() and 0xFFFF
        val poolHeaderSize = buffer.short.toInt() and 0xFFFF
        val poolChunkSize = buffer.int
        val stringCount = buffer.int

        assertEquals("RES_STRING_POOL_TYPE must be 0x0001", 0x0001, poolType)
        assertEquals("Pool header size must be 28", 28, poolHeaderSize)
        assertTrue("String count must be > 0", stringCount > 0)
        assertTrue("Pool chunk size must fit within file size", poolChunkSize < totalFileSize)

        // Verify idempotent re-modification (can modify the clone again to .c2)
        val secondClonePackage = "com.twinspace.cloner.c2"
        val secondModifiedBytes = AxmlEditor.modifyManifest(
            manifestBytes = modifiedBytes,
            originalPackage = targetPackage,
            targetPackage = secondClonePackage
        )

        assertNotNull(secondModifiedBytes)
        val secondBuffer = ByteBuffer.wrap(secondModifiedBytes).order(ByteOrder.LITTLE_ENDIAN)
        val secondXmlType = secondBuffer.short.toInt() and 0xFFFF
        val secondHeaderSize = secondBuffer.short.toInt() and 0xFFFF
        val secondFileSize = secondBuffer.int
        assertEquals("Re-modified AXML must preserve RES_XML_TYPE", 0x0003, secondXmlType)
        assertEquals("Re-modified header size must be 8", 8, secondHeaderSize)
        assertEquals("Re-modified file size must match byte array length", secondModifiedBytes.size, secondFileSize)
    }

    @Test
    fun testStringTransformationLogic() {
        val origPkg = "com.whatsapp"
        val clonePkg = "com.whatsapp.c1"

        val testStrings = listOf(
            "com.whatsapp",
            "com.whatsapp.fileprovider",
            "com.whatsapp.permission.MAPS_RECEIVE",
            "com.whatsapp_something",
            "android.intent.action.MAIN",
            "other.unrelated.package"
        )

        val modified = testStrings.map { str ->
            when {
                str == origPkg -> clonePkg
                str.startsWith("$origPkg.") -> str.replaceFirst(origPkg, clonePkg)
                str.contains(".$origPkg.") -> str.replace(".$origPkg.", ".$clonePkg.")
                str.startsWith("${origPkg}_") -> str.replaceFirst(origPkg, clonePkg)
                else -> str
            }
        }

        assertEquals("com.whatsapp.c1", modified[0])
        assertEquals("com.whatsapp.c1.fileprovider", modified[1])
        assertEquals("com.whatsapp.c1.permission.MAPS_RECEIVE", modified[2])
        assertEquals("com.whatsapp.c1_something", modified[3])
        assertEquals("android.intent.action.MAIN", modified[4])
        assertEquals("other.unrelated.package", modified[5])
    }
}
