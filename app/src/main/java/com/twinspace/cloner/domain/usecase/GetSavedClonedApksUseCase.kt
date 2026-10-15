package com.twinspace.cloner.domain.usecase

import com.twinspace.cloner.domain.model.ClonedApk
import com.twinspace.cloner.domain.repository.StandaloneCloneRepository

class GetSavedClonedApksUseCase(
    private val repository: StandaloneCloneRepository
) {
    suspend operator fun invoke(): List<ClonedApk> {
        return repository.getSavedClonedApks()
    }
}
