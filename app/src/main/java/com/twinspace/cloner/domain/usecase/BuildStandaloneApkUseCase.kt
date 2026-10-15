package com.twinspace.cloner.domain.usecase

import com.twinspace.cloner.domain.model.CloneTaskStatus
import com.twinspace.cloner.domain.repository.StandaloneCloneRepository
import java.io.File

class BuildStandaloneApkUseCase(
    private val repository: StandaloneCloneRepository
) {
    suspend operator fun invoke(
        originalPackage: String,
        cloneNumber: Int,
        onStatus: (CloneTaskStatus) -> Unit
    ): Result<File> {
        require(originalPackage.isNotBlank()) { "Original package name cannot be blank" }
        require(cloneNumber >= 1) { "Clone number must be at least 1" }
        return repository.buildStandaloneApk(originalPackage, cloneNumber, onStatus)
    }
}
