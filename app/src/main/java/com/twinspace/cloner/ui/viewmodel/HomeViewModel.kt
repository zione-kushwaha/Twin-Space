package com.twinspace.cloner.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twinspace.cloner.core.AppItem
import com.twinspace.cloner.core.ScreenTab
import com.twinspace.cloner.domain.model.CloneTaskStatus
import com.twinspace.cloner.domain.model.ClonedApk
import com.twinspace.cloner.domain.model.StorageUsageSummary
import com.twinspace.cloner.domain.repository.SecurityRepository
import com.twinspace.cloner.domain.repository.ShortcutRepository
import com.twinspace.cloner.domain.repository.StandaloneCloneRepository
import com.twinspace.cloner.domain.repository.StorageOptimizerRepository
import com.twinspace.cloner.domain.repository.VirtualCloneRepository
import com.twinspace.cloner.domain.usecase.BuildStandaloneApkUseCase
import com.twinspace.cloner.domain.usecase.CleanTemporaryCacheUseCase
import com.twinspace.cloner.domain.usecase.DeleteApkUseCase
import com.twinspace.cloner.domain.usecase.GetSavedClonedApksUseCase
import com.twinspace.cloner.domain.usecase.GetStorageUsageUseCase
import com.twinspace.cloner.domain.usecase.InstallApkUseCase
import com.twinspace.cloner.domain.usecase.PinShortcutUseCase
import com.twinspace.cloner.domain.usecase.ShareApkUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val clonedApps: List<AppItem> = emptyList(),
    val installedApps: List<AppItem> = emptyList(),
    val savedApks: List<ClonedApk> = emptyList(),
    val selectedTab: ScreenTab = ScreenTab.CLONED_APPS,
    val selectedCategory: String = "All",
    val buildTaskStatus: CloneTaskStatus = CloneTaskStatus.Idle,
    val isTwinProfileActive: Boolean = false,
    val storageSummary: StorageUsageSummary = StorageUsageSummary(),
    val isCleaningStorage: Boolean = false,
    val userNotification: String? = null
)

sealed interface HomeUiEvent {
    data class SelectTab(val tab: ScreenTab) : HomeUiEvent
    data class SelectCategory(val category: String) : HomeUiEvent
    data class StartStandaloneClone(val app: AppItem, val autoShare: Boolean = false) : HomeUiEvent
    data class InstallApk(val apk: ClonedApk) : HomeUiEvent
    data class ShareApk(val apk: ClonedApk) : HomeUiEvent
    data class DeleteApk(val apk: ClonedApk) : HomeUiEvent
    data class PinShortcut(val app: AppItem, val customName: String? = null, val cloneIndex: Int = 1) : HomeUiEvent
    object DismissBuildDialog : HomeUiEvent
    object RefreshData : HomeUiEvent
    object LoadStorageUsage : HomeUiEvent
    object CleanStorageCache : HomeUiEvent
    object ClearNotification : HomeUiEvent
}

class HomeViewModel(
    private val virtualCloneRepository: VirtualCloneRepository,
    private val standaloneCloneRepository: StandaloneCloneRepository,
    private val shortcutRepository: ShortcutRepository? = null,
    private val storageOptimizerRepository: StorageOptimizerRepository? = null,
    private val getSavedClonedApksUseCase: GetSavedClonedApksUseCase = GetSavedClonedApksUseCase(standaloneCloneRepository),
    private val buildStandaloneApkUseCase: BuildStandaloneApkUseCase = BuildStandaloneApkUseCase(standaloneCloneRepository),
    private val installApkUseCase: InstallApkUseCase = InstallApkUseCase(standaloneCloneRepository),
    private val shareApkUseCase: ShareApkUseCase = ShareApkUseCase(standaloneCloneRepository),
    private val deleteApkUseCase: DeleteApkUseCase = DeleteApkUseCase(standaloneCloneRepository),
    private val pinShortcutUseCase: PinShortcutUseCase = PinShortcutUseCase(shortcutRepository ?: object : ShortcutRepository {
        override fun isShortcutPinningSupported(): Boolean = false
        override fun pinAppShortcut(app: AppItem, customName: String?, cloneIndex: Int): Boolean = false
    }),
    private val getStorageUsageUseCase: GetStorageUsageUseCase? = storageOptimizerRepository?.let { GetStorageUsageUseCase(it) },
    private val cleanTemporaryCacheUseCase: CleanTemporaryCacheUseCase? = storageOptimizerRepository?.let { CleanTemporaryCacheUseCase(it) }
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refreshData()
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.SelectTab -> {
                _uiState.update { it.copy(selectedTab = event.tab) }
            }
            is HomeUiEvent.SelectCategory -> {
                _uiState.update { it.copy(selectedCategory = event.category) }
            }
            is HomeUiEvent.StartStandaloneClone -> {
                startStandaloneClone(event.app, event.autoShare)
            }
            is HomeUiEvent.InstallApk -> {
                installApkUseCase(event.apk.file)
            }
            is HomeUiEvent.ShareApk -> {
                shareApkUseCase(event.apk.file, event.apk.appName)
            }
            is HomeUiEvent.DeleteApk -> {
                deleteApk(event.apk)
            }
            is HomeUiEvent.PinShortcut -> {
                val success = pinShortcutUseCase(event.app, event.customName, event.cloneIndex)
                val msg = if (success) "Pinned ${event.app.appName} to Home Screen" else "Requested shortcut"
                _uiState.update { it.copy(userNotification = msg) }
            }
            is HomeUiEvent.DismissBuildDialog -> {
                _uiState.update { it.copy(buildTaskStatus = CloneTaskStatus.Idle) }
            }
            is HomeUiEvent.RefreshData -> {
                refreshData()
            }
            is HomeUiEvent.LoadStorageUsage -> {
                loadStorageUsage()
            }
            is HomeUiEvent.CleanStorageCache -> {
                cleanStorageCache()
            }
            is HomeUiEvent.ClearNotification -> {
                _uiState.update { it.copy(userNotification = null) }
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            val cloned = virtualCloneRepository.getClonedApps()
            val installed = virtualCloneRepository.getInstalledApps()
            val savedApks = getSavedClonedApksUseCase()
            val profileActive = virtualCloneRepository.isTwinProfileActive()

            _uiState.update {
                it.copy(
                    clonedApps = cloned,
                    installedApps = installed,
                    savedApks = savedApks,
                    isTwinProfileActive = profileActive
                )
            }
        }
    }

    private fun loadStorageUsage() {
        viewModelScope.launch {
            getStorageUsageUseCase?.let { useCase ->
                val summary = useCase()
                _uiState.update { it.copy(storageSummary = summary) }
            }
        }
    }

    private fun cleanStorageCache() {
        viewModelScope.launch {
            cleanTemporaryCacheUseCase?.let { useCase ->
                _uiState.update { it.copy(isCleaningStorage = true) }
                val freed = useCase()
                val summary = getStorageUsageUseCase?.invoke() ?: StorageUsageSummary()
                _uiState.update {
                    it.copy(
                        isCleaningStorage = false,
                        storageSummary = summary,
                        userNotification = "Freed ${StorageUsageSummary.formatBytes(freed)} cache space"
                    )
                }
            }
        }
    }

    private fun startStandaloneClone(app: AppItem, autoShare: Boolean) {
        viewModelScope.launch {
            val nextCloneNum = (_uiState.value.clonedApps.count {
                it.packageName == app.packageName && it.cloneIndex > 0
            } + 1).coerceAtLeast(1)

            _uiState.update {
                it.copy(
                    buildTaskStatus = CloneTaskStatus.InProgress(
                        step = CloneTaskStatus.PipelineStep.EXTRACTING,
                        percent = 0.1f,
                        message = "Extracting ${app.appName} APK & assets..."
                    )
                )
            }

            val result = buildStandaloneApkUseCase(
                originalPackage = app.packageName,
                cloneNumber = nextCloneNum,
                onStatus = { status ->
                    _uiState.update { it.copy(buildTaskStatus = status) }
                }
            )

            result.onSuccess { apkFile ->
                refreshData()
                if (autoShare) {
                    shareApkUseCase(apkFile, app.appName)
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        buildTaskStatus = CloneTaskStatus.Error(
                            message = error.localizedMessage ?: "Failed to generate standalone APK",
                            throwable = error
                        )
                    )
                }
            }
        }
    }

    private fun deleteApk(apk: ClonedApk) {
        viewModelScope.launch {
            val deleted = deleteApkUseCase(apk.file)
            if (deleted) {
                _uiState.update { it.copy(userNotification = "Deleted ${apk.fileName}") }
                refreshData()
            }
        }
    }
}
