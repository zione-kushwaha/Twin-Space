package com.twinspace.cloner.ui.screens

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.Shortcut
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import com.twinspace.cloner.TwinSpaceApp
import com.twinspace.cloner.domain.model.ClonedApk
import com.twinspace.cloner.domain.model.CloneTaskStatus
import com.twinspace.cloner.domain.model.StorageUsageSummary
import com.twinspace.cloner.ui.components.BuildPipelineModal
import com.twinspace.cloner.ui.components.PopularAppsQuickCloneBanner
import com.twinspace.cloner.ui.components.PrivacyTrustCenterModal
import com.twinspace.cloner.ui.components.StandaloneApksDashboard
import com.twinspace.cloner.ui.components.StorageOptimizerModal
import com.twinspace.cloner.core.StandaloneApkCloner
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.graphics.drawable.Drawable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.twinspace.cloner.R
import com.twinspace.cloner.core.AppItem
import com.twinspace.cloner.core.CloneSettings
import com.twinspace.cloner.core.DualSpaceManager
import com.twinspace.cloner.core.PreferencesManager
import com.twinspace.cloner.core.ScreenTab
import com.twinspace.cloner.ui.components.AddAppGridTile
import com.twinspace.cloner.ui.components.AppGridTile
import com.twinspace.cloner.ui.components.drawableToBitmap
import com.twinspace.cloner.ui.theme.TwinBg
import com.twinspace.cloner.ui.theme.TwinBorder
import com.twinspace.cloner.ui.theme.TwinDanger
import com.twinspace.cloner.ui.theme.TwinPrimary
import com.twinspace.cloner.ui.theme.TwinPrimarySoft
import com.twinspace.cloner.ui.theme.TwinSuccess
import com.twinspace.cloner.ui.theme.TwinSurface
import com.twinspace.cloner.ui.theme.TwinSurfaceAlt
import com.twinspace.cloner.ui.theme.TwinTextMuted
import com.twinspace.cloner.ui.theme.TwinTextPrimary
import com.twinspace.cloner.ui.theme.TwinTextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    dualSpaceManager: DualSpaceManager,
    prefs: PreferencesManager,
    clonedApps: List<AppItem>,
    installedApps: List<AppItem>,
    onLaunchApp: (AppItem) -> Unit,
    onCloneApp: (AppItem, CloneSettings?) -> Unit,
    onRemoveClone: (AppItem) -> Unit,
    onRemoveAllClones: () -> Unit = {},
    onSetupProfile: () -> Unit,
    onLockApp: () -> Unit,
    onShowOnboarding: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currentTab by remember { mutableStateOf(ScreenTab.CLONED_APPS) }
    var showAppPicker by remember { mutableStateOf(false) }
    var selectedAppForOptions by remember { mutableStateOf<AppItem?>(null) }
    var isWebContainerActive by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("All") }
    var showStorageModal by remember { mutableStateOf(false) }
    var showTrustModal by remember { mutableStateOf(false) }
    var storageSummary by remember { mutableStateOf(StorageUsageSummary()) }
    var isCleaningStorage by remember { mutableStateOf(false) }

    val loadStorageSummary = {
        scope.launch {
            storageSummary = TwinSpaceApp.instance.storageOptimizerRepository.calculateStorageUsage()
        }
    }

    val cleanStorageCache = {
        scope.launch {
            isCleaningStorage = true
            val freedBytes = TwinSpaceApp.instance.storageOptimizerRepository.cleanTemporaryCache()
            storageSummary = TwinSpaceApp.instance.storageOptimizerRepository.calculateStorageUsage()
            isCleaningStorage = false
            Toast.makeText(context, "Freed ${StorageUsageSummary.formatBytes(freedBytes)} cache space!", Toast.LENGTH_SHORT).show()
        }
    }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val standaloneCloner = remember { StandaloneApkCloner(context) }
    var savedApksVersion by remember { mutableStateOf(0) }
    val savedApks = remember(savedApksVersion) { standaloneCloner.getSavedClonedApks() }
    var standaloneTaskStatus by remember { mutableStateOf<CloneTaskStatus>(CloneTaskStatus.Idle) }
    var currentCloningApp by remember { mutableStateOf<AppItem?>(null) }

    val triggerStandaloneCloning: (AppItem, Boolean) -> Unit = { targetApp, autoShare ->
        currentCloningApp = targetApp
        scope.launch {
            val nextCloneNum = (clonedApps.count { it.packageName == targetApp.packageName && it.cloneIndex > 0 } + 1).coerceAtLeast(1)
            standaloneTaskStatus = CloneTaskStatus.InProgress(
                step = CloneTaskStatus.PipelineStep.EXTRACTING,
                percent = 0.1f,
                message = "Extracting ${targetApp.appName} APK & assets..."
            )
            val apkFile = standaloneCloner.cloneAndBuildApk(
                originalPackage = targetApp.packageName,
                cloneNumber = nextCloneNum,
                onStatus = { status ->
                    when (status) {
                        is StandaloneApkCloner.CloneStatus.Progress -> {
                            val step = when {
                                status.percent < 0.35f -> CloneTaskStatus.PipelineStep.EXTRACTING
                                status.percent < 0.70f -> CloneTaskStatus.PipelineStep.REWRITING
                                status.percent < 1.0f -> CloneTaskStatus.PipelineStep.SIGNING
                                else -> CloneTaskStatus.PipelineStep.COMPLETED
                            }
                            standaloneTaskStatus = CloneTaskStatus.InProgress(step, status.percent, status.message)
                        }
                        is StandaloneApkCloner.CloneStatus.Success -> {
                            savedApksVersion++
                            standaloneTaskStatus = CloneTaskStatus.Success(
                                apkFile = status.apkFile,
                                appName = targetApp.appName,
                                cloneNumber = nextCloneNum
                            )
                            if (autoShare) {
                                standaloneCloner.shareApk(status.apkFile, targetApp.appName)
                            } else {
                                if (standaloneCloner.canInstallApks()) {
                                    standaloneCloner.installApk(status.apkFile)
                                } else {
                                    context.startActivity(standaloneCloner.getInstallPermissionIntent())
                                }
                            }
                        }
                        is StandaloneApkCloner.CloneStatus.Error -> {
                            standaloneTaskStatus = CloneTaskStatus.Error(status.error)
                        }
                    }
                }
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TwinBg)
    ) {
        // TwinSpace Light Top Bar (hidden when Web Twin browser active)
        if (!isWebContainerActive) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TwinSurface)
                    .border(width = 1.dp, color = TwinBorder)
                    .padding(top = 10.dp, start = 16.dp, end = 16.dp, bottom = 10.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_twinspace_brand),
                            contentDescription = "TwinSpace Logo",
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "TwinSpace",
                                    color = TwinTextPrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 19.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(TwinPrimarySoft, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "PRO",
                                        color = TwinPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (dualSpaceManager.isTwinProfileActive()) TwinSuccess else TwinPrimary)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (dualSpaceManager.isTwinProfileActive()) "Sandbox Active" else "Ready to Clone",
                                    color = TwinTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Quick Actions (Trust Center, Storage Optimizer, Clear All & Lock)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { showTrustModal = true },
                            modifier = Modifier
                                .size(36.dp)
                                .background(TwinSurfaceAlt, CircleShape)
                                .border(1.dp, TwinBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = "Trust Center",
                                tint = TwinSuccess,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                loadStorageSummary()
                                showStorageModal = true
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(TwinSurfaceAlt, CircleShape)
                                .border(1.dp, TwinBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = "Storage Optimizer",
                                tint = TwinPrimary,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        if (clonedApps.isNotEmpty()) {
                            IconButton(
                                onClick = { showDeleteAllDialog = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(TwinSurfaceAlt, CircleShape)
                                    .border(1.dp, TwinBorder, CircleShape)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_trash_brand),
                                    contentDescription = "Clear All Clones",
                                    tint = TwinDanger,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onLockApp,
                            modifier = Modifier
                                .size(36.dp)
                                .background(TwinSurfaceAlt, CircleShape)
                                .border(1.dp, TwinBorder, CircleShape)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lock_brand),
                                contentDescription = "Lock",
                                tint = TwinTextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Segmented Tab Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TwinSurfaceAlt, RoundedCornerShape(12.dp))
                        .border(1.dp, TwinBorder, RoundedCornerShape(12.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    SegmentTabItem(
                        title = "Dual Apps",
                        badge = if (clonedApps.isNotEmpty()) "${clonedApps.size}" else null,
                        iconRes = R.drawable.ic_apps_brand,
                        selected = currentTab == ScreenTab.CLONED_APPS,
                        onClick = { currentTab = ScreenTab.CLONED_APPS },
                        modifier = Modifier.weight(1f)
                    )
                    SegmentTabItem(
                        title = "Standalone",
                        badge = if (savedApks.isNotEmpty()) "${savedApks.size}" else null,
                        iconRes = R.drawable.ic_clone_sparkle,
                        selected = currentTab == ScreenTab.STANDALONE_APKS,
                        onClick = { currentTab = ScreenTab.STANDALONE_APKS },
                        modifier = Modifier.weight(1f)
                    )
                    SegmentTabItem(
                        title = "Web Twin",
                        badge = "Web",
                        iconRes = R.drawable.ic_public_brand,
                        selected = currentTab == ScreenTab.WEB_TWINS,
                        onClick = { currentTab = ScreenTab.WEB_TWINS },
                        modifier = Modifier.weight(1f)
                    )
                    SegmentTabItem(
                        title = "Settings",
                        badge = null,
                        iconRes = R.drawable.ic_settings_brand,
                        selected = currentTab == ScreenTab.SETTINGS,
                        onClick = { currentTab = ScreenTab.SETTINGS },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Body Content
        Box(
            modifier = if (isWebContainerActive) {
                Modifier.fillMaxSize()
            } else {
                Modifier.weight(1f).fillMaxWidth()
            }
        ) {
            when (currentTab) {
                ScreenTab.CLONED_APPS -> {
                    val categories = listOf("All", "Social", "Google & Media", "Tools")
                    val filteredApps = remember(clonedApps, selectedCategory) {
                        when (selectedCategory) {
                            "Social" -> clonedApps.filter { app ->
                                val pkg = app.packageName.lowercase()
                                val name = app.appName.lowercase()
                                pkg.contains("whatsapp") || pkg.contains("telegram") || pkg.contains("facebook") ||
                                pkg.contains("instagram") || pkg.contains("twitter") || pkg.contains("snapchat") ||
                                pkg.contains("discord") || pkg.contains("linkedin") || pkg.contains("reddit") ||
                                pkg.contains("tiktok") || pkg.contains("messenger") || pkg.contains("viber") ||
                                pkg.contains("signal") || name.contains("chat") || name.contains("message")
                            }
                            "Google & Media" -> clonedApps.filter { app ->
                                val pkg = app.packageName.lowercase()
                                pkg.contains("google") || pkg.contains("chrome") || pkg.contains("youtube") ||
                                pkg.contains("photo") || pkg.contains("media") || pkg.contains("music") ||
                                pkg.contains("video") || pkg.contains("camera") || pkg.contains("gallery")
                            }
                            "Tools" -> clonedApps.filter { app ->
                                val pkg = app.packageName.lowercase()
                                !(pkg.contains("whatsapp") || pkg.contains("telegram") || pkg.contains("facebook") ||
                                  pkg.contains("instagram") || pkg.contains("twitter") || pkg.contains("snapchat") ||
                                  pkg.contains("discord") || pkg.contains("linkedin") || pkg.contains("reddit") ||
                                  pkg.contains("tiktok") || pkg.contains("messenger") || pkg.contains("viber") ||
                                  pkg.contains("signal") || pkg.contains("google") || pkg.contains("chrome") ||
                                  pkg.contains("youtube") || pkg.contains("photo") || pkg.contains("media"))
                            }
                            else -> clonedApps
                        }
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Profile Setup Banner if Work Profile is not active yet
                        if (!dualSpaceManager.isTwinProfileActive()) {
                            item(span = { GridItemSpan(3) }) {
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = TwinSurface),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, TwinBorder, RoundedCornerShape(14.dp))
                                        .padding(bottom = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(TwinPrimarySoft),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_shield_brand),
                                                contentDescription = null,
                                                tint = TwinPrimary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Enable Isolated Sandbox",
                                                color = TwinTextPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = "1-tap setup for independent WhatsApp & social accounts",
                                                color = TwinTextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = onSetupProfile,
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = TwinPrimary,
                                                contentColor = Color.White
                                            ),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("Setup", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // Category Filter Chips (shown when many apps are cloned)
                        if (clonedApps.size >= 8) {
                            item(span = { GridItemSpan(3) }) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    categories.forEach { category ->
                                        val count = when (category) {
                                            "All" -> clonedApps.size
                                            "Social" -> clonedApps.count { app ->
                                                val pkg = app.packageName.lowercase()
                                                val name = app.appName.lowercase()
                                                pkg.contains("whatsapp") || pkg.contains("telegram") || pkg.contains("facebook") ||
                                                pkg.contains("instagram") || pkg.contains("twitter") || pkg.contains("snapchat") ||
                                                pkg.contains("discord") || pkg.contains("linkedin") || pkg.contains("reddit") ||
                                                pkg.contains("tiktok") || pkg.contains("messenger") || pkg.contains("viber") ||
                                                pkg.contains("signal") || name.contains("chat") || name.contains("message")
                                            }
                                            "Google & Media" -> clonedApps.count { app ->
                                                val pkg = app.packageName.lowercase()
                                                pkg.contains("google") || pkg.contains("chrome") || pkg.contains("youtube") ||
                                                pkg.contains("photo") || pkg.contains("media") || pkg.contains("music") ||
                                                pkg.contains("video") || pkg.contains("camera") || pkg.contains("gallery")
                                            }
                                            "Tools" -> clonedApps.count { app ->
                                                val pkg = app.packageName.lowercase()
                                                !(pkg.contains("whatsapp") || pkg.contains("telegram") || pkg.contains("facebook") ||
                                                  pkg.contains("instagram") || pkg.contains("twitter") || pkg.contains("snapchat") ||
                                                  pkg.contains("discord") || pkg.contains("linkedin") || pkg.contains("reddit") ||
                                                  pkg.contains("tiktok") || pkg.contains("messenger") || pkg.contains("viber") ||
                                                  pkg.contains("signal") || pkg.contains("google") || pkg.contains("chrome") ||
                                                  pkg.contains("youtube") || pkg.contains("photo") || pkg.contains("media"))
                                            }
                                            else -> 0
                                        }

                                        val isSelected = selectedCategory == category
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { selectedCategory = category },
                                            label = {
                                                Text(
                                                    text = "$category ($count)",
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = TwinPrimarySoft,
                                                selectedLabelColor = TwinPrimary,
                                                containerColor = TwinSurface,
                                                labelColor = TwinTextSecondary
                                            ),
                                            border = FilterChipDefaults.filterChipBorder(
                                                enabled = true,
                                                selected = isSelected,
                                                borderColor = if (isSelected) TwinPrimary else TwinBorder
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Empty State if no apps are cloned yet
                        if (clonedApps.isEmpty()) {
                            item(span = { GridItemSpan(3) }) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    PopularAppsQuickCloneBanner(
                                        installedApps = installedApps,
                                        clonedApps = clonedApps,
                                        onClonePackage = { pkg ->
                                            val installedApp = installedApps.find { it.packageName == pkg }
                                                ?: AppItem(packageName = pkg, appName = pkg)
                                            onCloneApp(installedApp, null)
                                        }
                                    )

                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = TwinSurface),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, TwinBorder, RoundedCornerShape(16.dp))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(CircleShape)
                                                    .background(TwinPrimarySoft),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_apps_brand),
                                                    contentDescription = null,
                                                    tint = TwinPrimary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = "Clone Any Installed App",
                                                color = TwinTextPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Select from all ${installedApps.size} apps installed on your device",
                                                color = TwinTextSecondary,
                                                fontSize = 12.sp
                                            )
                                            Spacer(modifier = Modifier.height(14.dp))
                                            Button(
                                                onClick = { showAppPicker = true },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = TwinPrimary)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Browse Installed Apps (${installedApps.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Grid of Cloned Apps (Parallel Space launcher grid style)
                        items(filteredApps, key = { "${it.packageName}_${it.cloneIndex}" }) { app ->
                            AppGridTile(
                                app = app,
                                onClick = {
                                    Toast.makeText(context, "Opening ${app.appName}...", Toast.LENGTH_SHORT).show()
                                    onLaunchApp(app)
                                },
                                onLongClick = {
                                    selectedAppForOptions = app
                                }
                            )
                        }

                        // Permanent '+' Add App Tile in Grid
                        item {
                            AddAppGridTile(
                                onClick = { showAppPicker = true }
                            )
                        }
                    }
                }

                ScreenTab.STANDALONE_APKS -> {
                    StandaloneApksDashboard(
                        savedApks = savedApks.map { ClonedApk(file = it) },
                        installedApps = installedApps,
                        onInstallApk = { apk ->
                            if (standaloneCloner.canInstallApks()) {
                                standaloneCloner.installApk(apk.file)
                            } else {
                                context.startActivity(standaloneCloner.getInstallPermissionIntent())
                            }
                        },
                        onShareApk = { apk ->
                            standaloneCloner.shareApk(apk.file, apk.appName)
                        },
                        onDeleteApk = { apk ->
                            apk.file.delete()
                            savedApksVersion++
                            Toast.makeText(context, "Deleted ${apk.fileName}", Toast.LENGTH_SHORT).show()
                        },
                        onCreateNewClone = {
                            showAppPicker = true
                        }
                    )
                }

                ScreenTab.WEB_TWINS -> {
                    WebTwinScreen(
                        onActiveChanged = { isActive ->
                            isWebContainerActive = isActive
                        }
                    )
                }

                ScreenTab.SETTINGS -> {
                    SettingsScreen(
                        prefs = prefs,
                        onLockApp = onLockApp,
                        onShowOnboarding = onShowOnboarding,
                        onOpenStorageOptimizer = {
                            loadStorageSummary()
                            showStorageModal = true
                        },
                        onOpenTrustCenter = {
                            showTrustModal = true
                        }
                    )
                }
            }
        }
    }

    // Parallel Space style App Selection Modal
    if (showAppPicker) {
        AppSelectionDialog(
            installedApps = installedApps,
            onCloneApp = { app ->
                showAppPicker = false
                selectedCategory = "All"
                onCloneApp(app, null)
            },
            onInstallStandalone = { app ->
                showAppPicker = false
                triggerStandaloneCloning(app, false)
            },
            onDismiss = { showAppPicker = false }
        )
    }

    // App Options Bottom Sheet (Parallel Space long-press sheet)
    selectedAppForOptions?.let { app ->
        ModalBottomSheet(
            onDismissRequest = { selectedAppForOptions = null },
            sheetState = bottomSheetState,
            containerColor = TwinSurface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // App Header in Sheet
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (app.icon != null) {
                        val bitmap = drawableToBitmap(app.icon)
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = app.appName,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = app.appName,
                            color = TwinTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        val isDefault = app.cloneIndex == 0 || !app.isCloned
                        Text(
                            text = if (isDefault) "${app.packageName} • Main Device App" else "${app.packageName} • Clone #${app.cloneIndex}",
                            color = TwinTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                val isDefault = app.cloneIndex == 0 || !app.isCloned

                // Sheet Action: Launch
                SheetActionRow(
                    icon = Icons.Default.PlayArrow,
                    title = if (isDefault) "Open Main App" else "Open Cloned App",
                    subtitle = if (isDefault) "Launch primary device instance" else "Launch secondary isolated instance #${app.cloneIndex}",
                    iconTint = TwinPrimary,
                    onClick = {
                        selectedAppForOptions = null
                        onLaunchApp(app)
                    }
                )

                // Sheet Action: Create Another Clone
                val nextCloneNum = clonedApps.count { it.packageName == app.packageName && it.cloneIndex > 0 } + 1
                SheetActionRow(
                    icon = Icons.Default.Add,
                    title = "Clone Another in TwinSpace",
                    subtitle = "Add another private clone in space (#$nextCloneNum)",
                    iconTint = TwinPrimary,
                    onClick = {
                        selectedAppForOptions = null
                        selectedCategory = "All"
                        onCloneApp(app, null)
                    }
                )

                // Sheet Action: Install as Standalone System App
                SheetActionRow(
                    icon = Icons.Default.Download,
                    title = "Install as Standalone System App",
                    subtitle = "Installs directly into phone's system app drawer",
                    iconTint = TwinPrimary,
                    onClick = {
                        val target = app
                        selectedAppForOptions = null
                        triggerStandaloneCloning(target, false)
                    }
                )

                // Sheet Action: Share Cloned APK
                SheetActionRow(
                    icon = Icons.Default.Share,
                    title = "Export / Share Cloned APK",
                    subtitle = "Send standalone .apk file to other people",
                    iconTint = TwinSuccess,
                    onClick = {
                        val target = app
                        selectedAppForOptions = null
                        triggerStandaloneCloning(target, true)
                    }
                )

                // Sheet Action: Add to Home Screen (Desktop Shortcut)
                SheetActionRow(
                    icon = Icons.AutoMirrored.Filled.Shortcut,
                    title = "Add to Home Screen",
                    subtitle = "Place 1-tap badged shortcut directly on phone launcher",
                    iconTint = TwinPrimary,
                    onClick = {
                        val created = TwinSpaceApp.instance.shortcutRepository.pinAppShortcut(app, app.appName, app.cloneIndex)
                        Toast.makeText(
                            context,
                            if (created) "Added ${app.appName} shortcut to Home Screen!" else "Pinned shortcut requested",
                            Toast.LENGTH_SHORT
                        ).show()
                        selectedAppForOptions = null
                    }
                )

                // Sheet Action: Delete Clone
                SheetActionRow(
                    icon = Icons.Default.Delete,
                    title = if (isDefault) "Remove All from TwinSpace" else "Remove this Clone",
                    subtitle = if (isDefault) "Remove ${app.appName} and all its clones" else "Delete clone #${app.cloneIndex}",
                    iconTint = TwinDanger,
                    titleColor = TwinDanger,
                    onClick = {
                        selectedAppForOptions = null
                        onRemoveClone(app)
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Clear All Confirmation Dialog
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = {
                Text(
                    text = "Clear All Cloned Apps?",
                    fontWeight = FontWeight.Bold,
                    color = TwinTextPrimary
                )
            },
            text = {
                Text(
                    text = "This will remove all ${clonedApps.size} apps from the TwinSpace launcher list. You can re-add them anytime using the '+' button.",
                    color = TwinTextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAllDialog = false
                        onRemoveAllClones()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TwinDanger),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete All", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Cancel", color = TwinTextSecondary)
                }
            },
            containerColor = TwinSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Clean Architecture Modular Build Pipeline Modal
    BuildPipelineModal(
        status = standaloneTaskStatus,
        targetAppName = currentCloningApp?.appName ?: "App",
        targetAppIcon = currentCloningApp?.icon,
        onDismiss = { standaloneTaskStatus = CloneTaskStatus.Idle },
        onInstallApk = { apk ->
            if (standaloneCloner.canInstallApks()) {
                standaloneCloner.installApk(apk)
            } else {
                context.startActivity(standaloneCloner.getInstallPermissionIntent())
            }
        },
        onShareApk = { apk, appName ->
            standaloneCloner.shareApk(apk, appName)
        }
    )

    // Storage and Cache Optimizer Bottom Sheet
    if (showStorageModal) {
        StorageOptimizerModal(
            summary = storageSummary,
            isCleaning = isCleaningStorage,
            onCleanCache = { cleanStorageCache() },
            onDismiss = { showStorageModal = false }
        )
    }

    // Zero Data Collection Privacy Trust Center Bottom Sheet
    if (showTrustModal) {
        PrivacyTrustCenterModal(
            onDismiss = { showTrustModal = false }
        )
    }
}

@Composable
private fun SheetActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color,
    titleColor: Color = TwinTextPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(TwinSurfaceAlt, RoundedCornerShape(10.dp))
                .border(1.dp, TwinBorder, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = titleColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Text(
                text = subtitle,
                color = TwinTextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun SegmentTabItem(
    title: String,
    badge: String?,
    iconRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) TwinSurface else Color.Transparent,
        animationSpec = tween(180),
        label = "tabBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) TwinPrimary else TwinTextSecondary,
        animationSpec = tween(180),
        label = "tabContent"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(vertical = 7.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                tint = contentColor,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = title,
                color = if (selected) TwinTextPrimary else TwinTextSecondary,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp,
                maxLines = 1
            )
            if (badge != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .background(
                            if (selected) TwinPrimarySoft else TwinSurfaceAlt,
                            CircleShape
                        )
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = badge,
                        color = if (selected) TwinPrimary else TwinTextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

