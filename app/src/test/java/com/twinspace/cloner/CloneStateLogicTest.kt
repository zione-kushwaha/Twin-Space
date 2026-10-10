package com.twinspace.cloner

import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.text.DecimalFormat

class CloneStateLogicTest {

    @Test
    fun testMultiInstanceCloneCount() {
        // Simulates WhatsApp with 1 default + 3 clones = 4 instances total
        val clonedList = listOf(
            DummyAppItem("com.whatsapp", "WhatsApp", cloneIndex = 0, isCloned = false),
            DummyAppItem("com.whatsapp", "WhatsApp", cloneIndex = 1, isCloned = true),
            DummyAppItem("com.whatsapp", "WhatsApp", cloneIndex = 2, isCloned = true),
            DummyAppItem("com.whatsapp", "WhatsApp", cloneIndex = 3, isCloned = true),
            DummyAppItem("com.amibeats", "Ami Beats", cloneIndex = 1, isCloned = true)
        )

        val whatsAppTotal = clonedList.count { it.packageName == "com.whatsapp" }
        assertEquals(4, whatsAppTotal)

        val whatsAppClonesOnly = clonedList.count { it.packageName == "com.whatsapp" && it.cloneIndex > 0 }
        assertEquals(3, whatsAppClonesOnly)

        val nextIndex = whatsAppClonesOnly + 1
        assertEquals(4, nextIndex)
    }

    @Test
    fun testFileSizeFormatting() {
        fun formatFileSize(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
            val df = DecimalFormat("#,##0.#")
            return "${df.format(bytes / Math.pow(1024.0, digitGroups.toDouble()))} ${units[digitGroups]}"
        }

        assertEquals("500 B", formatFileSize(500L))
        assertEquals("10 KB", formatFileSize(10240L))
        assertEquals("48.5 MB", formatFileSize(50855936L))
        assertEquals("1.2 GB", formatFileSize(1288490188L))
    }

    @Test
    fun testCategoryClassification() {
        fun classify(packageName: String, appName: String): String {
            val pkg = packageName.lowercase()
            val name = appName.lowercase()
            return when {
                pkg.contains("whatsapp") || pkg.contains("telegram") || pkg.contains("facebook") ||
                pkg.contains("instagram") || pkg.contains("twitter") || pkg.contains("tiktok") ||
                pkg.contains("musically") || name.contains("tiktok") ||
                name.contains("chat") || name.contains("message") -> "Social"

                pkg.contains("google") || pkg.contains("chrome") || pkg.contains("youtube") ||
                pkg.contains("music") || pkg.contains("media") || name.contains("beats") -> "Media"

                else -> "Tools"
            }
        }

        assertEquals("Social", classify("com.whatsapp", "WhatsApp"))
        assertEquals("Social", classify("org.telegram.messenger", "Telegram"))
        assertEquals("Social", classify("com.zhiliaoapp.musically", "TikTok"))
        assertEquals("Media", classify("com.amibeats", "Ami Beats"))
        assertEquals("Media", classify("com.google.android.youtube", "YouTube"))
        assertEquals("Tools", classify("com.calculator.vault", "Calculator Vault"))
    }

    private data class DummyAppItem(
        val packageName: String,
        val appName: String,
        val cloneIndex: Int,
        val isCloned: Boolean
    )
}
