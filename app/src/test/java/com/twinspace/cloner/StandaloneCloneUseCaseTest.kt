package com.twinspace.cloner

import com.twinspace.cloner.domain.model.CloneTaskStatus
import com.twinspace.cloner.domain.model.ClonedApk
import com.twinspace.cloner.domain.repository.StandaloneCloneRepository
import com.twinspace.cloner.domain.usecase.BuildStandaloneApkUseCase
import com.twinspace.cloner.domain.usecase.DeleteApkUseCase
import com.twinspace.cloner.domain.usecase.GetSavedClonedApksUseCase
import com.twinspace.cloner.domain.usecase.InstallApkUseCase
import com.twinspace.cloner.domain.usecase.ShareApkUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class StandaloneCloneUseCaseTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private class FakeStandaloneCloneRepository(
        val existingFiles: MutableList<File> = mutableListOf()
    ) : StandaloneCloneRepository {

        var lastInstalledFile: File? = null
        var lastSharedFile: File? = null
        var lastSharedAppName: String? = null

        override suspend fun getSavedClonedApks(): List<ClonedApk> {
            return existingFiles.map { ClonedApk(file = it) }
        }

        override suspend fun buildStandaloneApk(
            originalPackage: String,
            cloneNumber: Int,
            onStatus: (CloneTaskStatus) -> Unit
        ): Result<File> {
            onStatus(CloneTaskStatus.InProgress(CloneTaskStatus.PipelineStep.EXTRACTING, 0.2f, "Extracting"))
            onStatus(CloneTaskStatus.InProgress(CloneTaskStatus.PipelineStep.REWRITING, 0.5f, "Rewriting"))
            onStatus(CloneTaskStatus.InProgress(CloneTaskStatus.PipelineStep.SIGNING, 0.8f, "Signing"))

            val dummyFile = File("fake/path/${originalPackage}_Clone$cloneNumber.apk")
            onStatus(CloneTaskStatus.Success(dummyFile, originalPackage, cloneNumber))
            return Result.success(dummyFile)
        }

        override fun installApk(apkFile: File): Result<Unit> {
            lastInstalledFile = apkFile
            return Result.success(Unit)
        }

        override fun shareApk(apkFile: File, appName: String): Result<Unit> {
            lastSharedFile = apkFile
            lastSharedAppName = appName
            return Result.success(Unit)
        }

        override fun deleteApk(apkFile: File): Boolean {
            return existingFiles.remove(apkFile)
        }

        override fun canInstallApks(): Boolean = true
    }

    @Test
    fun testGetSavedClonedApksUseCase() = runBlocking {
        val file1 = tempFolder.newFile("App1_Clone1.apk")
        val file2 = tempFolder.newFile("App2_Clone2.apk")

        val fakeRepo = FakeStandaloneCloneRepository(mutableListOf(file1, file2))
        val useCase = GetSavedClonedApksUseCase(fakeRepo)

        val apks = useCase()
        assertEquals(2, apks.size)
        assertEquals("App1", apks[0].appName)
        assertEquals("App2", apks[1].appName)
    }

    @Test
    fun testBuildStandaloneApkUseCaseValidation() = runBlocking {
        val fakeRepo = FakeStandaloneCloneRepository()
        val useCase = BuildStandaloneApkUseCase(fakeRepo)

        try {
            useCase("", 1) {}
            fail("Blank package should throw IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("package"))
        }

        try {
            useCase("com.example", 0) {}
            fail("Clone number 0 should throw IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Clone number"))
        }

        val statuses = mutableListOf<CloneTaskStatus>()
        val result = useCase("com.whatsapp", 1) { statuses.add(it) }
        assertTrue(result.isSuccess)
        assertEquals(4, statuses.size)
        assertTrue(statuses.last() is CloneTaskStatus.Success)
    }

    @Test
    fun testInstallAndShareUseCasesValidation() {
        val fakeRepo = FakeStandaloneCloneRepository()
        val installUseCase = InstallApkUseCase(fakeRepo)
        val shareUseCase = ShareApkUseCase(fakeRepo)

        val nonExistent = File(tempFolder.root, "ghost.apk")
        val installFail = installUseCase(nonExistent)
        assertTrue(installFail.isFailure)

        val shareFail = shareUseCase(nonExistent, "Ghost")
        assertTrue(shareFail.isFailure)

        val realFile = tempFolder.newFile("real.apk").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        val installSuccess = installUseCase(realFile)
        assertTrue(installSuccess.isSuccess)
        assertEquals(realFile, fakeRepo.lastInstalledFile)

        val shareSuccess = shareUseCase(realFile, "RealApp")
        assertTrue(shareSuccess.isSuccess)
        assertEquals(realFile, fakeRepo.lastSharedFile)
        assertEquals("RealApp", fakeRepo.lastSharedAppName)
    }

    @Test
    fun testDeleteApkUseCase() {
        val file = tempFolder.newFile("to_delete.apk")
        val fakeRepo = FakeStandaloneCloneRepository(mutableListOf(file))
        val deleteUseCase = DeleteApkUseCase(fakeRepo)

        val deleted = deleteUseCase(file)
        assertTrue(deleted)
        assertEquals(0, fakeRepo.existingFiles.size)
    }
}
