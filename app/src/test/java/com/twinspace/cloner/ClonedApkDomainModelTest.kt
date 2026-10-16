package com.twinspace.cloner

import com.twinspace.cloner.domain.model.ClonedApk
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ClonedApkDomainModelTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testParseAppNameAndCloneNumber() {
        assertEquals("WhatsApp", ClonedApk.parseAppName("WhatsApp_Clone1.apk"))
        assertEquals(1, ClonedApk.parseCloneNumber("WhatsApp_Clone1.apk"))

        assertEquals("Ami Beats", ClonedApk.parseAppName("Ami_Beats_Clone2.apk"))
        assertEquals(2, ClonedApk.parseCloneNumber("Ami_Beats_Clone2.apk"))

        assertEquals("TikTok Lite", ClonedApk.parseAppName("TikTok_Lite_Clone3.apk"))
        assertEquals(3, ClonedApk.parseCloneNumber("TikTok_Lite_Clone3.apk"))

        assertEquals("Cloned App", ClonedApk.parseAppName(".apk"))
        assertEquals(1, ClonedApk.parseCloneNumber(".apk"))
    }

    @Test
    fun testFileSizeFormatting() {
        assertEquals("0 B", ClonedApk.formatFileSize(0L))
        assertEquals("512 B", ClonedApk.formatFileSize(512L))
        assertEquals("1 KB", ClonedApk.formatFileSize(1024L))
        assertEquals("2.5 MB", ClonedApk.formatFileSize((2.5 * 1024 * 1024).toLong()))
        assertEquals("1.5 GB", ClonedApk.formatFileSize((1.5 * 1024 * 1024 * 1024).toLong()))
    }

    @Test
    fun testClonedApkEntityProperties() {
        val testFile = tempFolder.newFile("Telegram_Clone4.apk").apply {
            writeBytes(ByteArray(2048))
        }

        val apk = ClonedApk(file = testFile)
        assertEquals("Telegram_Clone4.apk", apk.fileName)
        assertEquals("Telegram", apk.appName)
        assertEquals(4, apk.cloneNumber)
        assertEquals(2048L, apk.sizeBytes)
        assertEquals("2 KB", apk.formattedSize)
        assertTrue(apk.formattedDate.isNotBlank())
        assertNotEquals("Unknown", apk.formattedDate)
    }
}
