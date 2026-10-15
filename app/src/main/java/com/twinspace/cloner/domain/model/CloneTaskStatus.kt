package com.twinspace.cloner.domain.model

import java.io.File

/**
 * Domain pipeline state for standalone APK building, rewriting, signing, and installation.
 */
sealed interface CloneTaskStatus {
    object Idle : CloneTaskStatus

    data class InProgress(
        val step: PipelineStep,
        val percent: Float,
        val message: String
    ) : CloneTaskStatus

    data class Success(
        val apkFile: File,
        val appName: String,
        val cloneNumber: Int
    ) : CloneTaskStatus

    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : CloneTaskStatus

    enum class PipelineStep(val title: String, val stepIndex: Int) {
        EXTRACTING("Extract", 1),
        REWRITING("Rewrite", 2),
        SIGNING("Sign v1-v3", 3),
        COMPLETED("Ready", 4)
    }
}
