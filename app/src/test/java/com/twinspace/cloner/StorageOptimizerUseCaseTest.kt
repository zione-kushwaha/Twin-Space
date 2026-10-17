package com.twinspace.cloner

import com.twinspace.cloner.domain.model.StorageUsageSummary
import com.twinspace.cloner.domain.repository.StorageOptimizerRepository
import com.twinspace.cloner.domain.usecase.CleanTemporaryCacheUseCase
import com.twinspace.cloner.domain.usecase.GetStorageUsageUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class StorageOptimizerUseCaseTest {

    private class FakeStorageOptimizerRepo : StorageOptimizerRepository {
        var currentSummary = StorageUsageSummary(
            apkFilesSizeBytes = 8_000_000L,
            cacheSizeBytes = 4_000_000L,
            tempFilesSizeBytes = 2_000_000L
        )

        override suspend fun calculateStorageUsage(): StorageUsageSummary = currentSummary

        override suspend fun cleanTemporaryCache(): Long {
            val freed = currentSummary.cacheSizeBytes + currentSummary.tempFilesSizeBytes
            currentSummary = currentSummary.copy(cacheSizeBytes = 0L, tempFilesSizeBytes = 0L)
            return freed
        }
    }

    @Test
    fun testGetStorageUsageUseCase() = runBlocking {
        val repo = FakeStorageOptimizerRepo()
        val getUsage = GetStorageUsageUseCase(repo)

        val summary = getUsage()
        assertEquals(14_000_000L, summary.totalSizeBytes)
        assertEquals(8_000_000L, summary.apkFilesSizeBytes)
    }

    @Test
    fun testCleanTemporaryCacheUseCase() = runBlocking {
        val repo = FakeStorageOptimizerRepo()
        val cleanCache = CleanTemporaryCacheUseCase(repo)
        val getUsage = GetStorageUsageUseCase(repo)

        val freed = cleanCache()
        assertEquals(6_000_000L, freed)

        val updated = getUsage()
        assertEquals(0L, updated.cacheSizeBytes)
        assertEquals(0L, updated.tempFilesSizeBytes)
        assertEquals(8_000_000L, updated.totalSizeBytes)
    }
}
