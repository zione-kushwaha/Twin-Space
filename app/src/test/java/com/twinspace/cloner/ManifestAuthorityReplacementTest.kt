package com.twinspace.cloner

import com.twinspace.cloner.core.AxmlEditor
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ManifestAuthorityReplacementTest {

    @Test
    fun testAuthorityReplacementWithDeepSubdomains() {
        // Construct a realistic AXML binary chunk with multiple provider authorities
        val originalStrings = listOf(
            "http://schemas.android.com/apk/res/android",
            "manifest",
            "package",
            "com.social.network",
            "provider",
            "authorities",
            "com.social.network.provider",
            "com.social.network.fileprovider",
            "com.social.network.analytics.provider",
            "com.social.network_extra_unrelated",
            "name",
            "androidx.core.content.FileProvider"
        )

        val axmlBytes = createMinimalAxmlChunk(originalStrings)

        val modifiedBytes = AxmlEditor.modifyManifest(
            manifestBytes = axmlBytes,
            originalPackage = "com.social.network",
            targetPackage = "com.social.network.clone2"
        )

        assertNotNull(modifiedBytes)
        assertTrue(modifiedBytes.isNotEmpty())

        val modifiedStrings = parseStringsFromAxml(modifiedBytes)

        // Package should be modified
        assertTrue(modifiedStrings.contains("com.social.network.clone2"))
        assertFalse(modifiedStrings.contains("com.social.network"))

        // Authorities should be modified with new package prefix
        assertTrue(modifiedStrings.contains("com.social.network.clone2.provider"))
        assertTrue(modifiedStrings.contains("com.social.network.clone2.fileprovider"))
        assertTrue(modifiedStrings.contains("com.social.network.clone2.analytics.provider"))

        // Unmodified strings should remain intact
        assertTrue(modifiedStrings.contains("androidx.core.content.FileProvider"))
        assertTrue(modifiedStrings.contains("manifest"))
        assertTrue(modifiedStrings.contains("provider"))
    }

    @Test
    fun testPaddingAlignmentMultipleOfFour() {
        val originalStrings = listOf(
            "odd_string_1",
            "odd_string_two",
            "com.target.odd",
            "com.target.odd.auth"
        )
        val axmlBytes = createMinimalAxmlChunk(originalStrings)

        val modifiedBytes = AxmlEditor.modifyManifest(
            manifestBytes = axmlBytes,
            originalPackage = "com.target.odd",
            targetPackage = "com.target.odd.cloned123"
        )

        // AXML chunk size must be divisible by 4
        assertEquals(0, modifiedBytes.size % 4)
    }

    private fun parseStringsFromAxml(bytes: ByteArray): List<String> {
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        buffer.position(8) // Skip XML header
        val poolType = buffer.short.toInt() and 0xFFFF
        val poolHeaderSize = buffer.short.toInt() and 0xFFFF
        val poolChunkSize = buffer.int
        val stringCount = buffer.int
        val styleCount = buffer.int
        val flags = buffer.int
        val stringsStart = buffer.int
        val stylesStart = buffer.int

        val offsets = IntArray(stringCount) { buffer.int }
        val stringsDataStart = 8 + stringsStart

        val list = mutableListOf<String>()
        val isUtf8 = (flags and 0x0100) != 0

        for (offset in offsets) {
            val pos = stringsDataStart + offset
            if (isUtf8) {
                var p = pos
                val charLen = bytes[p++].toInt() and 0xFF
                val byteLen = bytes[p++].toInt() and 0xFF
                val str = String(bytes, p, byteLen, Charsets.UTF_8)
                list.add(str)
            } else {
                var p = pos
                val len = (bytes[p].toInt() and 0xFF) or ((bytes[p + 1].toInt() and 0xFF) shl 8)
                val str = String(bytes, p + 2, len * 2, Charsets.UTF_16LE)
                list.add(str)
            }
        }
        return list
    }

    private fun createMinimalAxmlChunk(strings: List<String>): ByteArray {
        val stringPoolData = ByteArrayOutputStream()
        val offsets = mutableListOf<Int>()

        var currentOffset = 0
        for (str in strings) {
            offsets.add(currentOffset)
            val utf8Bytes = str.toByteArray(Charsets.UTF_8)
            val charLen = str.length
            val byteLen = utf8Bytes.size

            stringPoolData.write(charLen and 0xFF)
            stringPoolData.write(byteLen and 0xFF)
            stringPoolData.write(utf8Bytes)
            stringPoolData.write(0) // Null terminator
            currentOffset += 2 + byteLen + 1
        }

        // Pad strings data to 4-byte boundary
        val unpaddedSize = stringPoolData.size()
        val pad = (4 - (unpaddedSize % 4)) % 4
        for (i in 0 until pad) {
            stringPoolData.write(0)
        }
        val stringBytes = stringPoolData.toByteArray()

        val stringOffsetsLen = strings.size * 4
        val stringsStart = 28 + stringOffsetsLen
        val poolHeaderSize = 28
        val poolChunkSize = poolHeaderSize + stringOffsetsLen + stringBytes.size

        val poolBuf = ByteBuffer.allocate(poolChunkSize).order(ByteOrder.LITTLE_ENDIAN)
        poolBuf.putShort(0x0001.toShort()) // RES_STRING_POOL_TYPE
        poolBuf.putShort(poolHeaderSize.toShort())
        poolBuf.putInt(poolChunkSize)
        poolBuf.putInt(strings.size)
        poolBuf.putInt(0) // style count
        poolBuf.putInt(1 shl 8) // flags UTF8
        poolBuf.putInt(stringsStart)
        poolBuf.putInt(0) // styles start

        for (off in offsets) {
            poolBuf.putInt(off)
        }
        poolBuf.put(stringBytes)

        val poolByteArray = poolBuf.array()
        val totalFileSize = 8 + poolByteArray.size

        val headerBuf = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN)
        headerBuf.putShort(0x0003.toShort()) // RES_XML_TYPE
        headerBuf.putShort(8.toShort())      // header size
        headerBuf.putInt(totalFileSize)

        val finalBytes = ByteArrayOutputStream()
        finalBytes.write(headerBuf.array())
        finalBytes.write(poolByteArray)
        return finalBytes.toByteArray()
    }
}
