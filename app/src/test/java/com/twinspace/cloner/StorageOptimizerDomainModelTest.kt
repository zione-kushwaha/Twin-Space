package com.twinspace.cloner

import com.twinspace.cloner.domain.model.StorageUsageSummary
import org.junit.Assert.assertEquals
import org.junit.Test

class StorageOptimizerDomainModelTest {

    @Test
    fun testStorageUsageSummaryCalculation() {
        val summary = StorageUsageSummary(
            apkFilesSizeBytes = 10_000_000L,
            cacheSizeBytes = 5_000_000L,
            tempFilesSizeBytes = 2_000_000L
        )

        assertEquals(17_000_000L, summary.totalSizeBytes)
    }

    @Test
    fun testFormatBytesFormatting() {
        assertEquals("0.0 MB", StorageUsageSummary.formatBytes(0L))
        assertEquals("500 B", StorageUsageSummary.formatBytes(500L))
        assertEquals("10.0 KB", StorageUsageSummary.formatBytes(10240L))
        assertEquals("10.0 MB", StorageUsageSummary.formatBytes(10485760L))
        assertEquals("2.00 GB", StorageUsageSummary.formatBytes(2147483648L))
    }
}
