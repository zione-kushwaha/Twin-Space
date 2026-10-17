package com.twinspace.cloner

import com.twinspace.cloner.core.AppItem
import com.twinspace.cloner.core.CloneSettings
import com.twinspace.cloner.core.ScreenTab
import com.twinspace.cloner.domain.model.CloneTaskStatus
import com.twinspace.cloner.domain.model.ClonedApk
import com.twinspace.cloner.domain.repository.StandaloneCloneRepository
import com.twinspace.cloner.domain.repository.VirtualCloneRepository
import com.twinspace.cloner.ui.viewmodel.HomeUiEvent
import com.twinspace.cloner.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeVirtualCloneRepo : VirtualCloneRepository {
        val clonedList = mutableListOf<AppItem>()
        val installedList = mutableListOf<AppItem>()
        var profileActive = false

        override fun getClonedApps(): List<AppItem> = clonedList
        override fun getInstalledApps(): List<AppItem> = installedList
        override fun cloneApp(packageName: String, settings: CloneSettings?): Boolean {
            clonedList.add(AppItem(packageName, packageName))
            return true
        }
        override fun removeClone(app: AppItem): Boolean {
            return clonedList.remove(app)
        }
        override fun removeAllClones() {
            clonedList.clear()
        }
        override fun launchApp(app: AppItem) {}
        override fun isTwinProfileActive(): Boolean = profileActive
    }

    private class FakeStandaloneRepo : StandaloneCloneRepository {
        val savedList = mutableListOf<File>()

        override suspend fun getSavedClonedApks(): List<ClonedApk> {
            return savedList.map { ClonedApk(file = it) }
        }

        override suspend fun buildStandaloneApk(
            originalPackage: String,
            cloneNumber: Int,
            onStatus: (CloneTaskStatus) -> Unit
        ): Result<File> {
            val file = File("test_${originalPackage}_Clone$cloneNumber.apk")
            savedList.add(file)
            onStatus(CloneTaskStatus.Success(file, originalPackage, cloneNumber))
            return Result.success(file)
        }

        override fun installApk(apkFile: File): Result<Unit> = Result.success(Unit)
        override fun shareApk(apkFile: File, appName: String): Result<Unit> = Result.success(Unit)
        override fun deleteApk(apkFile: File): Boolean = savedList.remove(apkFile)
        override fun canInstallApks(): Boolean = true
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testTabAndCategorySelection() = runTest(testDispatcher) {
        val virtualRepo = FakeVirtualCloneRepo()
        val standaloneRepo = FakeStandaloneRepo()
        val viewModel = HomeViewModel(virtualRepo, standaloneRepo)

        testScheduler.advanceUntilIdle()

        assertEquals(ScreenTab.CLONED_APPS, viewModel.uiState.value.selectedTab)
        assertEquals("All", viewModel.uiState.value.selectedCategory)

        viewModel.onEvent(HomeUiEvent.SelectTab(ScreenTab.STANDALONE_APKS))
        assertEquals(ScreenTab.STANDALONE_APKS, viewModel.uiState.value.selectedTab)

        viewModel.onEvent(HomeUiEvent.SelectCategory("Social"))
        assertEquals("Social", viewModel.uiState.value.selectedCategory)
    }

    @Test
    fun testStartStandaloneCloneAndDismissDialog() = runTest(testDispatcher) {
        val virtualRepo = FakeVirtualCloneRepo()
        val standaloneRepo = FakeStandaloneRepo()
        val viewModel = HomeViewModel(virtualRepo, standaloneRepo)

        testScheduler.advanceUntilIdle()

        val targetApp = AppItem("com.whatsapp", "WhatsApp", cloneIndex = 0)
        viewModel.onEvent(HomeUiEvent.StartStandaloneClone(targetApp, autoShare = false))

        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.buildTaskStatus is CloneTaskStatus.Success)
        assertEquals(1, viewModel.uiState.value.savedApks.size)

        viewModel.onEvent(HomeUiEvent.DismissBuildDialog)
        assertEquals(CloneTaskStatus.Idle, viewModel.uiState.value.buildTaskStatus)
    }

    @Test
    fun testDeleteStandaloneApk() = runTest(testDispatcher) {
        val virtualRepo = FakeVirtualCloneRepo()
        val standaloneRepo = FakeStandaloneRepo()
        val testFile = File("WhatsApp_Clone1.apk")
        standaloneRepo.savedList.add(testFile)

        val viewModel = HomeViewModel(virtualRepo, standaloneRepo)
        testScheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.savedApks.size)

        viewModel.onEvent(HomeUiEvent.DeleteApk(ClonedApk(testFile)))
        testScheduler.advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.savedApks.size)
        assertTrue(viewModel.uiState.value.userNotification!!.contains("Deleted"))
    }

    @Test
    fun testPinShortcutEvent() = runTest(testDispatcher) {
        val virtualRepo = FakeVirtualCloneRepo()
        val standaloneRepo = FakeStandaloneRepo()
        val fakeShortcutRepo = object : com.twinspace.cloner.domain.repository.ShortcutRepository {
            override fun isShortcutPinningSupported(): Boolean = true
            override fun pinAppShortcut(app: AppItem, customName: String?, cloneIndex: Int): Boolean = true
        }

        val viewModel = HomeViewModel(
            virtualCloneRepository = virtualRepo,
            standaloneCloneRepository = standaloneRepo,
            shortcutRepository = fakeShortcutRepo
        )
        testScheduler.advanceUntilIdle()

        val app = AppItem("com.whatsapp", "WhatsApp", cloneIndex = 2)
        viewModel.onEvent(HomeUiEvent.PinShortcut(app, "WhatsApp #2", 2))

        assertTrue(viewModel.uiState.value.userNotification!!.contains("Pinned"))
    }
}
