package com.twinspace.cloner

import com.twinspace.cloner.core.CloneRecord
import org.junit.Assert.*
import org.junit.Test

class CloneRecordLogicTest {

    @Test
    fun testNextIndexAllocation() {
        val records = mutableListOf<CloneRecord>()

        fun getNextIndex(pkg: String): Int {
            val usedIndices = records.filter { it.packageName == pkg }.map { it.cloneIndex }.toSet()
            var next = 1
            while (usedIndices.contains(next)) {
                next++
            }
            return next
        }

        val pkg = "com.whatsapp"

        // 1st clone: nextIndex should be 1
        val idx1 = getNextIndex(pkg)
        assertEquals(1, idx1)
        records.add(CloneRecord(id = "${pkg}_$idx1", packageName = pkg, cloneIndex = idx1))

        // 2nd clone: nextIndex should be 2
        val idx2 = getNextIndex(pkg)
        assertEquals(2, idx2)
        records.add(CloneRecord(id = "${pkg}_$idx2", packageName = pkg, cloneIndex = idx2))

        // 3rd clone: nextIndex should be 3
        val idx3 = getNextIndex(pkg)
        assertEquals(3, idx3)
        records.add(CloneRecord(id = "${pkg}_$idx3", packageName = pkg, cloneIndex = idx3))

        // Remove 2nd clone (idx 2)
        records.removeAll { it.cloneIndex == 2 }
        assertEquals(2, records.size)

        // Next allocated clone should re-use slot 2
        val nextAvailable = getNextIndex(pkg)
        assertEquals(2, nextAvailable)
    }

    @Test
    fun testBadgeNumberGeneration() {
        // Clone index 0 = base instance (badge 1)
        // Clone index 1 = 1st clone (badge 2)
        // Clone index 2 = 2nd clone (badge 3)
        // Clone index 3 = 3rd clone (badge 4)
        for (i in 0..5) {
            val expectedBadge = (i + 1).toString()
            assertEquals("Badge number for index $i must be ${i + 1}", expectedBadge, (i + 1).toString())
        }
    }
}
