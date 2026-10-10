package com.twinspace.cloner.core

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import android.util.Log
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.twinspace.cloner.MainActivity
import com.twinspace.cloner.admin.TwinDeviceAdminReceiver

class DualSpaceManager(
    private val context: Context,
    private val prefs: PreferencesManager
) {
    private val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    private val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    private val packageManager = context.packageManager
    private val adminComponent = TwinDeviceAdminReceiver.getComponentName(context)

    /**
     * Finds the secondary / isolated user handle if one exists.
     */
    fun getTwinProfile(): UserHandle? {
        val myUser = Process.myUserHandle()
        for (profile in userManager.userProfiles) {
            if (profile != myUser) {
                return profile
            }
        }
        for (profile in launcherApps.profiles) {
            if (profile != myUser) {
                return profile
            }
        }
        return null
    }

    fun isTwinProfileActive(): Boolean {
        return getTwinProfile() != null
    }

    fun isProfileOwner(): Boolean {
        return dpm.isProfileOwnerApp(context.packageName)
    }

    /**
     * Completely removes the secondary sandbox / work profile from the device.
     */
    fun removeTwinProfile(): Boolean {
        return try {
            if (isProfileOwner()) {
                dpm.wipeData(0)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Intent to provision the isolated TwinSpace managed profile.
     */
    fun createProvisionIntent(): Intent {
        val intent = Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)
        intent.putExtra(
            DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
            adminComponent
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_SKIP_ENCRYPTION, true)
        }
        return intent
    }

    /**
     * Retrieves all launchable applications in the primary space.
     */
    /**
     * Checks if a package is cloned in the twin profile or multi-clone preferences.
     */
    fun isAppCloned(packageName: String): Boolean {
        return prefs.isAppCloned(packageName)
    }

    /**
     * Returns total number of active clone instances for this package.
     */
    fun getCloneCount(packageName: String): Int {
        return prefs.getCloneCount(packageName)
    }

    /**
     * Retrieves all launchable applications in the primary space.
     */
    fun getInstalledApps(): List<AppItem> {
        val myUser = Process.myUserHandle()
        val appList = mutableListOf<AppItem>()
        val seen = mutableSetOf<String>()

        try {
            val activityList = launcherApps.getActivityList(null, myUser)
            for (launcherActivity in activityList) {
                val pkg = launcherActivity.applicationInfo.packageName
                if (pkg == context.packageName || !seen.add(pkg)) continue

                val appName = launcherActivity.label.toString()
                val icon = launcherActivity.getBadgedIcon(0)
                val isCloned = isAppCloned(pkg)
                val cloneCount = getCloneCount(pkg)

                appList.add(
                    AppItem(
                        packageName = pkg,
                        appName = appName,
                        icon = icon,
                        isCloned = isCloned,
                        userHandle = myUser,
                        cloneIndex = cloneCount,
                        settings = CloneSettings(badgeText = if (cloneCount > 0) (cloneCount + 1).toString() else "2")
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("DualSpaceManager", "Error querying launcher activities: ${e.message}", e)
        }

        // Robust fallback using PackageManager
        if (appList.isEmpty()) {
            try {
                val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val resolveInfos = packageManager.queryIntentActivities(mainIntent, 0)
                for (ri in resolveInfos) {
                    val pkg = ri.activityInfo.packageName
                    if (pkg == context.packageName || !seen.add(pkg)) continue

                    val appName = ri.loadLabel(packageManager).toString()
                    val icon = ri.loadIcon(packageManager)
                    val isCloned = isAppCloned(pkg)
                    val cloneCount = getCloneCount(pkg)

                    appList.add(
                        AppItem(
                            packageName = pkg,
                            appName = appName,
                            icon = icon,
                            isCloned = isCloned,
                            userHandle = myUser,
                            cloneIndex = cloneCount,
                            settings = CloneSettings(badgeText = if (cloneCount > 0) (cloneCount + 1).toString() else "2")
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("DualSpaceManager", "Error querying package manager: ${e.message}", e)
            }
        }

        return appList.sortedBy { it.appName.lowercase() }
    }

    /**
     * Retrieves all cloned apps in TwinSpace:
     * For each cloned package:
     * 1. The Default (original / primary space) app (cloneIndex = 0, badge = "1")
     * 2. All cloned instances (cloneIndex = 1..N, badges = 2, 3, 4...)
     * For example, cloning WhatsApp 3 times displays 4 WhatsApp apps (1 default + 3 cloned).
     */
    fun getClonedApps(): List<AppItem> {
        val twinProfile = getTwinProfile()
        val clonedList = mutableListOf<AppItem>()
        val clonedPackages = mutableSetOf<String>()
        clonedPackages.addAll(prefs.getClonedPackages())
        val records = prefs.getCloneRecords()
        for (r in records) {
            clonedPackages.add(r.packageName)
        }

        // Only packages explicitly cloned by user are listed
        for (pkg in clonedPackages) {
            // Get default app label & icon from primary profile
            var defaultName = pkg
            var defaultIcon: Drawable? = null
            try {
                val appInfo = packageManager.getApplicationInfo(pkg, 0)
                defaultName = packageManager.getApplicationLabel(appInfo).toString()
                defaultIcon = packageManager.getApplicationIcon(appInfo)
            } catch (_: Exception) {
                try {
                    val myActs = launcherApps.getActivityList(pkg, Process.myUserHandle())
                    if (myActs.isNotEmpty()) {
                        defaultName = myActs[0].label.toString()
                        defaultIcon = myActs[0].getBadgedIcon(0)
                    }
                } catch (_: Exception) {}
            }

            var twinIcon = defaultIcon
            if (twinProfile != null) {
                try {
                    val acts = launcherApps.getActivityList(pkg, twinProfile)
                    if (acts.isNotEmpty()) {
                        twinIcon = acts[0].getBadgedIcon(0)
                        if (defaultName == pkg) {
                            defaultName = acts[0].label.toString()
                        }
                    }
                } catch (_: Exception) {}
            }

            // 1. DEFAULT / ORIGINAL APP (cloneIndex = 0, badgeText = "1", isCloned = false)
            clonedList.add(
                AppItem(
                    packageName = pkg,
                    appName = defaultName,
                    icon = defaultIcon ?: twinIcon,
                    isCloned = false,
                    userHandle = Process.myUserHandle(),
                    cloneIndex = 0,
                    settings = CloneSettings(
                        badgeText = "1",
                        customName = defaultName,
                        cloneNumber = 1
                    )
                )
            )

            // 2. CLONED INSTANCES (cloneIndex = 1..N, badges = 2, 3, 4...)
            val pkgRecords = records.filter { it.packageName == pkg }.sortedBy { it.cloneIndex }
            if (pkgRecords.isNotEmpty()) {
                for (record in pkgRecords) {
                    val badgeNumber = (record.cloneIndex + 1).toString()
                    val cloneTitle = if (record.customName.isNotBlank()) {
                        record.customName
                    } else {
                        "$defaultName ($badgeNumber)"
                    }

                    clonedList.add(
                        AppItem(
                            packageName = pkg,
                            appName = cloneTitle,
                            icon = twinIcon ?: defaultIcon,
                            isCloned = true,
                            userHandle = twinProfile,
                            cloneIndex = record.cloneIndex,
                            settings = CloneSettings(
                                customName = record.customName,
                                badgeText = if (record.badgeText.isNotBlank()) record.badgeText else badgeNumber,
                                cloneNumber = record.cloneIndex + 1
                            )
                        )
                    )
                }
            } else {
                // If package was marked cloned in twinProfile or preferences without sub-records, provide Clone #1
                clonedList.add(
                    AppItem(
                        packageName = pkg,
                        appName = "$defaultName (2)",
                        icon = twinIcon ?: defaultIcon,
                        isCloned = true,
                        userHandle = twinProfile,
                        cloneIndex = 1,
                        settings = CloneSettings(
                            badgeText = "2",
                            customName = "$defaultName (2)",
                            cloneNumber = 2
                        )
                    )
                )
            }
        }

        return clonedList.sortedWith(compareBy({ it.packageName.lowercase() }, { it.cloneIndex }))
    }

    /**
     * Clones an existing package into TwinSpace.
     * Supports multiple clones of the same app (Clone 1, Clone 2, Clone 3...).
     */
    fun cloneApp(packageName: String, settings: CloneSettings? = null): Boolean {
        val existingRecords = prefs.getCloneRecords().filter { it.packageName == packageName }
        val usedIndices = existingRecords.map { it.cloneIndex }.toMutableSet()

        // If package is already cloned in preferences or space without explicit sub-records, seed index 1
        if (existingRecords.isEmpty() && isAppCloned(packageName)) {
            val seedRecord = CloneRecord(
                id = "${packageName}_1",
                packageName = packageName,
                cloneIndex = 1,
                customName = "",
                badgeText = "2"
            )
            prefs.addCloneRecord(seedRecord)
            usedIndices.add(1)
        }

        var nextIndex = 1
        while (usedIndices.contains(nextIndex)) {
            nextIndex++
        }
        val badgeNumber = (nextIndex + 1).toString()

        val record = CloneRecord(
            id = "${packageName}_$nextIndex",
            packageName = packageName,
            cloneIndex = nextIndex,
            customName = settings?.customName ?: "",
            badgeText = settings?.badgeText?.ifBlank { badgeNumber } ?: badgeNumber
        )
        prefs.addCloneRecord(record)
        prefs.addClonedPackage(packageName)

        if (isProfileOwner()) {
            try {
                dpm.installExistingPackage(adminComponent, packageName)
                Log.d("DualSpaceManager", "Package $packageName installed into Twin Profile")
                return true
            } catch (e: Exception) {
                Log.e("DualSpaceManager", "Error installing package in profile: ${e.message}", e)
            }
        }
        return true
    }

    /**
     * Removes an app clone from TwinSpace.
     */
    fun removeClone(app: AppItem): Boolean {
        if (app.cloneIndex == 0) {
            // Removing the default card removes all clones of this package
            return removeClone(app.packageName)
        }
        prefs.removeCloneRecord("${app.packageName}_${app.cloneIndex}")
        val remaining = prefs.getCloneRecords().count { it.packageName == app.packageName }
        if (remaining <= 0) {
            prefs.removeClonedPackage(app.packageName)
        }
        return true
    }

    fun removeClone(packageName: String): Boolean {
        prefs.removeClonedPackage(packageName)
        val allForPkg = prefs.getCloneRecords().filter { it.packageName == packageName }
        for (r in allForPkg) {
            prefs.removeCloneRecord(r.id)
        }
        return true
    }

    /**
     * Deletes all cloned apps and resets the space.
     */
    fun removeAllClones(): Boolean {
        prefs.removeAllClones()
        return true
    }

    /**
     * Launches a cloned app inside the isolated Twin Profile or launches the default app in primary space.
     */
    fun launchApp(app: AppItem, onProfileRequired: () -> Unit = {}) {
        // If this is the DEFAULT / ORIGINAL app (cloneIndex == 0 or !isCloned)
        if (app.cloneIndex == 0 || !app.isCloned) {
            try {
                val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return
                } else {
                    val myActs = launcherApps.getActivityList(app.packageName, Process.myUserHandle())
                    if (myActs.isNotEmpty()) {
                        launcherApps.startMainActivity(myActs[0].componentName, Process.myUserHandle(), null, null)
                        return
                    }
                }
            } catch (e: Exception) {
                Log.e("DualSpaceManager", "Error launching default app: ${e.message}", e)
                android.widget.Toast.makeText(context, "Could not open ${app.appName}: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Isolated cloned instance
        val twinProfile = app.userHandle ?: getTwinProfile()

        if (twinProfile != null) {
            try {
                val activities = launcherApps.getActivityList(app.packageName, twinProfile)
                if (activities.isNotEmpty()) {
                    val comp = activities[0].componentName
                    launcherApps.startMainActivity(comp, twinProfile, null, null)
                    return
                }
            } catch (e: Exception) {
                Log.e("DualSpaceManager", "Error launching via profile: ${e.message}", e)
            }
        }

        // Multi-instance clone launch
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                launchIntent.putExtra("TWIN_CLONE_INDEX", app.cloneIndex)
                launchIntent.putExtra("TWIN_ISOLATED", true)
                context.startActivity(launchIntent)
                return
            }
            val myActs = launcherApps.getActivityList(app.packageName, Process.myUserHandle())
            if (myActs.isNotEmpty()) {
                launcherApps.startMainActivity(myActs[0].componentName, Process.myUserHandle(), null, null)
                return
            }
        } catch (e: Exception) {
            Log.e("DualSpaceManager", "Error launching cloned app: ${e.message}", e)
            android.widget.Toast.makeText(context, "Could not launch ${app.appName}: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Creates a pinned Home Screen shortcut for the cloned app with a "Twin" badge.
     */
    fun createTwinShortcut(app: AppItem): Boolean {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            return false
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("LAUNCH_CLONE_PKG", app.packageName)
            putExtra("LAUNCH_CLONE_INDEX", app.cloneIndex)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val badgeText = app.settings.badgeText.ifBlank { "2" }
        val iconBitmap = drawableToBitmapWithBadge(app.icon, badgeText)
        val iconCompat = IconCompat.createWithBitmap(iconBitmap)

        val displayName = if (app.appName.contains("(") || app.appName.endsWith("2")) {
            app.appName
        } else {
            "${app.appName} (2)"
        }

        val pinShortcutInfo = ShortcutInfoCompat.Builder(context, "twin_${app.packageName}_${app.cloneIndex}")
            .setShortLabel(if (displayName.length > 12) displayName.take(12) else displayName)
            .setLongLabel("$displayName - TwinSpace")
            .setIcon(iconCompat)
            .setIntent(launchIntent)
            .build()

        return ShortcutManagerCompat.requestPinShortcut(context, pinShortcutInfo, null)
    }

    private fun drawableToBitmapWithBadge(drawable: Drawable?, badgeText: String = "2"): Bitmap {
        val size = 144
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        if (drawable != null) {
            drawable.setBounds(0, 0, size, size)
            drawable.draw(canvas)
        } else {
            val bgPaint = Paint().apply { color = Color.parseColor("#1E293B") }
            canvas.drawCircle(size / 2f, size / 2f, size / 2f, bgPaint)
        }

        // Draw modern Twin badge on bottom right
        val badgeRadius = 26f
        val badgeCx = size - badgeRadius - 4
        val badgeCy = size - badgeRadius - 4

        val badgePaint = Paint().apply {
            color = Color.parseColor("#4F46E5")
            isAntiAlias = true
        }
        val borderPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 5f
            isAntiAlias = true
        }
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 22f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        canvas.drawCircle(badgeCx, badgeCy, badgeRadius, badgePaint)
        canvas.drawCircle(badgeCx, badgeCy, badgeRadius, borderPaint)
        canvas.drawText(badgeText, badgeCx, badgeCy + 8f, textPaint)

        return bitmap
    }
}
