package com.twinspace.cloner

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.twinspace.cloner.admin.TwinDeviceAdminReceiver

/**
 * Headless activity that runs inside the isolated Managed Profile (User 10)
 * to install packages into the profile sandbox via DevicePolicyManager.
 */
class CloneInstallerActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pkgToInstall = intent.getStringExtra("PACKAGE_TO_INSTALL")
        val pkgToRemove = intent.getStringExtra("PACKAGE_TO_REMOVE")

        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = TwinDeviceAdminReceiver.getComponentName(this)

        if (!pkgToInstall.isNullOrBlank()) {
            try {
                dpm.installExistingPackage(adminComponent, pkgToInstall)
                Log.d("CloneInstaller", "Successfully installed $pkgToInstall into isolated profile")
            } catch (e: Exception) {
                Log.e("CloneInstaller", "Failed to install $pkgToInstall in profile: ${e.message}", e)
            }
        }

        if (!pkgToRemove.isNullOrBlank()) {
            try {
                // Hide or suspend package if needed
                dpm.setApplicationHidden(adminComponent, pkgToRemove, true)
                Log.d("CloneInstaller", "Successfully hid $pkgToRemove in isolated profile")
            } catch (e: Exception) {
                Log.e("CloneInstaller", "Failed to hide $pkgToRemove: ${e.message}", e)
            }
        }

        finish()
        overridePendingTransition(0, 0)
    }
}
