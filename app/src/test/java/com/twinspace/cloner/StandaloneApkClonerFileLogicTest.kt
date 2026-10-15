package com.twinspace.cloner

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class StandaloneApkClonerFileLogicTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testApkFileNamingAndSanitization() {
        fun sanitize(appName: String, cloneNum: Int): String {
            val sanitizedName = appName.replace("[^a-zA-Z0-9]+".toRegex(), "_").trim('_').ifBlank { "ClonedApp" }
            return "${sanitizedName}_Clone$cloneNum.apk"
        }

        assertEquals("WhatsApp_Clone1.apk", sanitize("WhatsApp", 1))
        assertEquals("Ami_Beats_Clone2.apk", sanitize("Ami Beats", 2))
        assertEquals("TikTok_Lite_Clone3.apk", sanitize("TikTok (Lite)", 3))
        assertEquals("X_Clone1.apk", sanitize("X", 1))
        assertEquals("My_App_2_0_Clone5.apk", sanitize("My App 2.0!", 5))
        assertEquals("ClonedApp_Clone1.apk", sanitize("   !@#$%^&*()   ", 1))
    }

    @Test
    fun testSavedClonedApksSorting() {
        val rootDir = tempFolder.newFolder("clones")
        val pkgDir1 = File(rootDir, "com.amibeats").apply { mkdirs() }
        val pkgDir2 = File(rootDir, "com.whatsapp").apply { mkdirs() }

        val apk1 = File(pkgDir1, "Ami_Beats_Clone1.apk").apply {
            writeBytes(byteArrayOf(1, 2, 3))
            setLastModified(1000L)
        }
        val apk2 = File(pkgDir2, "WhatsApp_Clone1.apk").apply {
            writeBytes(byteArrayOf(4, 5, 6))
            setLastModified(3000L)
        }
        val apk3 = File(pkgDir1, "Ami_Beats_Clone2.apk").apply {
            writeBytes(byteArrayOf(7, 8, 9))
            setLastModified(2000L)
        }

        // Dummy non-apk file
        File(pkgDir1, "temp.txt").writeText("test")

        val apks = mutableListOf<File>()
        rootDir.walkTopDown().forEach { file ->
            if (file.isFile && file.extension.lowercase() == "apk") {
                apks.add(file)
            }
        }
        val sorted = apks.sortedByDescending { it.lastModified() }

        assertEquals(3, sorted.size)
        assertEquals("WhatsApp_Clone1.apk", sorted[0].name)
        assertEquals("Ami_Beats_Clone2.apk", sorted[1].name)
        assertEquals("Ami_Beats_Clone1.apk", sorted[2].name)
    }
}
