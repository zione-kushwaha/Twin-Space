package com.twinspace.cloner

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Modifier
import com.twinspace.cloner.core.AppItem
import com.twinspace.cloner.ui.screens.AppLockScreen
import com.twinspace.cloner.ui.screens.CalculatorDisguiseScreen
import com.twinspace.cloner.ui.screens.HomeScreen
import com.twinspace.cloner.ui.screens.OnboardingScreen
import com.twinspace.cloner.ui.theme.BgDark
import com.twinspace.cloner.ui.theme.TwinSpaceTheme

class MainActivity : ComponentActivity() {

    private val app by lazy { application as TwinSpaceApp }
    private val dualSpaceManager by lazy { app.dualSpaceManager }
    private val prefs by lazy { app.prefs }
    private val securityManager by lazy { app.securityManager }

    private val provisionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "TwinSpace Profile Setup Succeeded!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Profile Setup in progress or canceled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (handleShortcutIntent(intent)) {
            return
        }

        setContent {
            TwinSpaceTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = com.twinspace.cloner.ui.theme.TwinBg
                ) {
                    TwinSpaceAppContent()
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleShortcutIntent(intent)
    }

    private fun handleShortcutIntent(intent: android.content.Intent?): Boolean {
        if (intent == null) return false
        val clonePkg = intent.getStringExtra("LAUNCH_CLONE_PKG")
            ?: intent.getStringExtra("KEY_LAUNCH_PACKAGE")
        if (!clonePkg.isNullOrBlank()) {
            val cloneIndex = intent.getIntExtra(
                "LAUNCH_CLONE_INDEX",
                intent.getIntExtra("KEY_LAUNCH_CLONE_INDEX", 1)
            )
            val appToLaunch = dualSpaceManager.getClonedApps().find { it.packageName == clonePkg && it.cloneIndex == cloneIndex }
                ?: AppItem(packageName = clonePkg, appName = clonePkg, cloneIndex = cloneIndex, isCloned = true)
            dualSpaceManager.launchApp(appToLaunch)
            finish()
            return true
        }
        return false
    }

    @Composable
    private fun TwinSpaceAppContent() {
        var hasCompletedOnboarding by remember { mutableStateOf(prefs.hasCompletedOnboarding) }
        var showOnboardingGuide by remember { mutableStateOf(false) }
        var isLocked by remember { mutableStateOf(securityManager.isLocked()) }
        var clonedApps by remember { mutableStateOf(dualSpaceManager.getClonedApps()) }
        var installedApps by remember { mutableStateOf(dualSpaceManager.getInstalledApps()) }

        fun reloadData() {
            clonedApps = dualSpaceManager.getClonedApps()
            installedApps = dualSpaceManager.getInstalledApps()
        }

        val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                    reloadData()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        LaunchedEffect(Unit) {
            if (!prefs.hasCleanedDefaultSeeds) {
                dualSpaceManager.removeClone("com.whatsapp")
                dualSpaceManager.removeClone("org.telegram.messenger")
                prefs.hasCleanedDefaultSeeds = true
            }
            reloadData()
        }

        if (!hasCompletedOnboarding || showOnboardingGuide) {
            OnboardingScreen(
                defaultPin = prefs.secretPin,
                onComplete = {
                    prefs.hasCompletedOnboarding = true
                    hasCompletedOnboarding = true
                    showOnboardingGuide = false
                }
            )
        } else if (isLocked) {
            if (prefs.isDisguiseModeEnabled) {
                CalculatorDisguiseScreen(
                    defaultPinHint = prefs.secretPin,
                    onUnlock = { pinInput ->
                        val success = securityManager.verifyPin(pinInput)
                        if (success) {
                            isLocked = false
                            reloadData()
                            Toast.makeText(this@MainActivity, "TwinSpace Unlocked", Toast.LENGTH_SHORT).show()
                        }
                        success
                    }
                )
            } else {
                AppLockScreen(
                    onUnlock = { pinInput ->
                        val success = securityManager.verifyPin(pinInput)
                        if (success) {
                            isLocked = false
                            reloadData()
                            Toast.makeText(this@MainActivity, "TwinSpace Unlocked", Toast.LENGTH_SHORT).show()
                        }
                        success
                    }
                )
            }
        } else {
            HomeScreen(
                dualSpaceManager = dualSpaceManager,
                prefs = prefs,
                clonedApps = clonedApps,
                installedApps = installedApps,
                onLaunchApp = { appItem ->
                    dualSpaceManager.launchApp(
                        app = appItem,
                        onProfileRequired = {
                            Toast.makeText(this@MainActivity, "Twin Sandbox Profile required for 100% isolated 2nd account", Toast.LENGTH_LONG).show()
                            try {
                                provisionLauncher.launch(dualSpaceManager.createProvisionIntent())
                            } catch (e: Exception) {
                                Toast.makeText(this@MainActivity, "Profile setup error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                },
                onCloneApp = { appItem, settings ->
                    dualSpaceManager.cloneApp(appItem.packageName, settings)
                    reloadData()
                    val totalForPkg = dualSpaceManager.getClonedApps().count { it.packageName == appItem.packageName }
                    val displayName = if (settings?.customName.isNullOrBlank()) appItem.appName else settings?.customName
                    Toast.makeText(this@MainActivity, "$displayName added ($totalForPkg total instances)", Toast.LENGTH_SHORT).show()
                },
                onRemoveClone = { appItem ->
                    dualSpaceManager.removeClone(appItem)
                    reloadData()
                    Toast.makeText(this@MainActivity, "${appItem.appName} removed from TwinSpace", Toast.LENGTH_SHORT).show()
                },
                onRemoveAllClones = {
                    prefs.hasInitializedDefaults = true
                    dualSpaceManager.removeAllClones()
                    reloadData()
                    Toast.makeText(this@MainActivity, "All cloned apps removed", Toast.LENGTH_SHORT).show()
                },
                onSetupProfile = {
                    try {
                        provisionLauncher.launch(dualSpaceManager.createProvisionIntent())
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "Profile provisioning: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                onLockApp = {
                    securityManager.lockSession()
                    isLocked = true
                },
                onShowOnboarding = {
                    showOnboardingGuide = true
                }
            )
        }
    }
}
