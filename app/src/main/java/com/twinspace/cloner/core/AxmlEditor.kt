package com.twinspace.cloner.core

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Lightweight, high-performance binary AXML (Android Binary XML) manifest modifier.
 * Modifies package names, provider authorities, and permissions in compiled AndroidManifest.xml
 * while preserving all resource IDs, attributes, namespaces, and binary structures.
 */
object AxmlEditor {

    private const val RES_XML_TYPE = 0x0003
    private const val RES_STRING_POOL_TYPE = 0x0001
    private const val UTF8_FLAG = 0x00000100

    fun modifyManifest(
        manifestBytes: ByteArray,
        originalPackage: String,
        targetPackage: String
    ): ByteArray {
        val buffer = ByteBuffer.wrap(manifestBytes).order(ByteOrder.LITTLE_ENDIAN)

        // 1. File Header
        val xmlType = buffer.short.toInt() and 0xFFFF
        val xmlHeaderSize = buffer.short.toInt() and 0xFFFF
        val oldFileSize = buffer.int

        require(xmlType == RES_XML_TYPE) { "Not a valid AXML file (type: $xmlType)" }

        // 2. String Pool Chunk
        val poolStart = buffer.position()
        val poolType = buffer.short.toInt() and 0xFFFF
        val poolHeaderSize = buffer.short.toInt() and 0xFFFF
        val oldPoolChunkSize = buffer.int
        val stringCount = buffer.int
        val styleCount = buffer.int
        val flags = buffer.int
        val stringsStart = buffer.int
        val stylesStart = buffer.int

        require(poolType == RES_STRING_POOL_TYPE) { "String pool chunk missing (type: $poolType)" }

        val isUtf8 = (flags and UTF8_FLAG) != 0

        // Read string offsets
        val stringOffsets = IntArray(stringCount)
        for (i in 0 until stringCount) {
            stringOffsets[i] = buffer.int
        }

        // Read style offsets if present
        val styleOffsets = IntArray(styleCount)
        for (i in 0 until styleCount) {
            styleOffsets[i] = buffer.int
        }

        val stringDataAbsoluteStart = poolStart + stringsStart
        val styleDataAbsoluteStart = if (stylesStart != 0) poolStart + stylesStart else poolStart + oldPoolChunkSize

        // Decode strings
        val decodedStrings = ArrayList<String>(stringCount)
        for (i in 0 until stringCount) {
            val offset = stringDataAbsoluteStart + stringOffsets[i]
            buffer.position(offset)
            if (isUtf8) {
                // Read char length
                var charLen = buffer.get().toInt() and 0xFF
                if ((charLen and 0x80) != 0) {
                    charLen = ((charLen and 0x7F) shl 8) or (buffer.get().toInt() and 0xFF)
                }
                // Read byte length
                var byteLen = buffer.get().toInt() and 0xFF
                if ((byteLen and 0x80) != 0) {
                    byteLen = ((byteLen and 0x7F) shl 8) or (buffer.get().toInt() and 0xFF)
                }
                val strBytes = ByteArray(byteLen)
                buffer.get(strBytes)
                decodedStrings.add(String(strBytes, Charsets.UTF_8))
            } else {
                // UTF-16
                var charLen = buffer.short.toInt() and 0xFFFF
                if ((charLen and 0x8000) != 0) {
                    val low = buffer.short.toInt() and 0xFFFF
                    charLen = ((charLen and 0x7FFF) shl 16) or low
                }
                val byteLen = charLen * 2
                val strBytes = ByteArray(byteLen)
                buffer.get(strBytes)
                decodedStrings.add(String(strBytes, Charsets.UTF_16LE))
            }
        }

        // Preserve style data if any
        val styleDataBytes = if (styleCount > 0 && styleDataAbsoluteStart < poolStart + oldPoolChunkSize) {
            buffer.position(styleDataAbsoluteStart)
            val len = (poolStart + oldPoolChunkSize) - styleDataAbsoluteStart
            ByteArray(len).also { buffer.get(it) }
        } else {
            ByteArray(0)
        }

        // Capture the rest of the AXML file (Resource map, tags, attributes)
        val restOfFileStart = poolStart + oldPoolChunkSize
        buffer.position(restOfFileStart)
        val restOfFileBytes = ByteArray(manifestBytes.size - restOfFileStart)
        buffer.get(restOfFileBytes)

        // 3. Transform Strings: Replace package name & provider authorities
        val modifiedStrings = decodedStrings.map { str ->
            when {
                str == originalPackage -> targetPackage
                str.startsWith("$originalPackage.") -> str.replaceFirst(originalPackage, targetPackage)
                str.contains(".$originalPackage.") -> str.replace(".$originalPackage.", ".$targetPackage.")
                str.startsWith("${originalPackage}_") -> str.replaceFirst(originalPackage, targetPackage)
                else -> str
            }
        }

        // 4. Re-encode String Pool
        val newStringOffsets = IntArray(stringCount)
        val stringDataOut = ByteArrayOutputStream()

        for (i in 0 until stringCount) {
            newStringOffsets[i] = stringDataOut.size()
            val str = modifiedStrings[i]

            if (isUtf8) {
                val utf8Bytes = str.toByteArray(Charsets.UTF_8)
                val charLen = str.length
                val byteLen = utf8Bytes.size

                if (charLen > 0x7F) {
                    stringDataOut.write(((charLen shr 8) and 0x7F) or 0x80)
                    stringDataOut.write(charLen and 0xFF)
                } else {
                    stringDataOut.write(charLen)
                }

                if (byteLen > 0x7F) {
                    stringDataOut.write(((byteLen shr 8) and 0x7F) or 0x80)
                    stringDataOut.write(byteLen and 0xFF)
                } else {
                    stringDataOut.write(byteLen)
                }

                stringDataOut.write(utf8Bytes)
                stringDataOut.write(0) // Null terminator
            } else {
                val charLen = str.length
                if (charLen > 0x7FFF) {
                    val high = ((charLen shr 16) and 0x7FFF) or 0x8000
                    val low = charLen and 0xFFFF
                    stringDataOut.write(low and 0xFF)
                    stringDataOut.write((low shr 8) and 0xFF)
                    stringDataOut.write(high and 0xFF)
                    stringDataOut.write((high shr 8) and 0xFF)
                } else {
                    stringDataOut.write(charLen and 0xFF)
                    stringDataOut.write((charLen shr 8) and 0xFF)
                }

                stringDataOut.write(str.toByteArray(Charsets.UTF_16LE))
                stringDataOut.write(0)
                stringDataOut.write(0) // 2-byte null terminator
            }
        }

        // 4-byte alignment padding for string data
        val unpadded = stringDataOut.size()
        val padding = (4 - (unpadded % 4)) % 4
        for (p in 0 until padding) {
            stringDataOut.write(0)
        }
        val paddedStringData = stringDataOut.toByteArray()

        val newStringsStart = poolHeaderSize + (stringCount * 4) + (styleCount * 4)
        val newStylesStart = if (styleCount > 0) newStringsStart + paddedStringData.size else 0
        val newPoolChunkSize = newStringsStart + paddedStringData.size + styleDataBytes.size
        val newTotalFileSize = xmlHeaderSize + newPoolChunkSize + restOfFileBytes.size

        // 5. Assemble Output AXML
        val outBuffer = ByteBuffer.allocate(newTotalFileSize).order(ByteOrder.LITTLE_ENDIAN)

        // File Header
        outBuffer.putShort(RES_XML_TYPE.toShort())
        outBuffer.putShort(xmlHeaderSize.toShort())
        outBuffer.putInt(newTotalFileSize)

        // String Pool Header
        outBuffer.putShort(RES_STRING_POOL_TYPE.toShort())
        outBuffer.putShort(poolHeaderSize.toShort())
        outBuffer.putInt(newPoolChunkSize)
        outBuffer.putInt(stringCount)
        outBuffer.putInt(styleCount)
        outBuffer.putInt(flags)
        outBuffer.putInt(newStringsStart)
        outBuffer.putInt(newStylesStart)

        // String offsets
        for (offset in newStringOffsets) {
            outBuffer.putInt(offset)
        }

        // Style offsets
        for (offset in styleOffsets) {
            outBuffer.putInt(offset)
        }

        // Padded string data
        outBuffer.put(paddedStringData)

        // Style data
        if (styleDataBytes.isNotEmpty()) {
            outBuffer.put(styleDataBytes)
        }

        // Rest of file chunks
        outBuffer.put(restOfFileBytes)

        return outBuffer.array()
    }
}
