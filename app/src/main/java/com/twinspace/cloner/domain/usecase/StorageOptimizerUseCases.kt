package com.twinspace.cloner.domain.usecase

import com.twinspace.cloner.domain.model.StorageUsageSummary
import com.twinspace.cloner.domain.repository.StorageOptimizerRepository

class GetStorageUsageUseCase(
    private val repository: StorageOptimizerRepository
) {
    suspend operator fun invoke(): StorageUsageSummary = repository.calculateStorageUsage()
}

class CleanTemporaryCacheUseCase(
    private val repository: StorageOptimizerRepository
) {
    suspend operator fun invoke(): Long = repository.cleanTemporaryCache()
}
