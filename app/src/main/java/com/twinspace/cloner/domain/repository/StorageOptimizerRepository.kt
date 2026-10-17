package com.twinspace.cloner.domain.repository

import com.twinspace.cloner.domain.model.StorageUsageSummary

/**
 * Domain repository contract for calculating disk consumption and cleaning temporary clone cache.
 */
interface StorageOptimizerRepository {
    suspend fun calculateStorageUsage(): StorageUsageSummary
    suspend fun cleanTemporaryCache(): Long
}
