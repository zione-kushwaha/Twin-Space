package com.twinspace.cloner.admin

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log

class TwinDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = ComponentName(context, TwinDeviceAdminReceiver::class.java)

        try {
            // Enable profile and set profile name
            dpm.setProfileEnabled(admin)
            dpm.setProfileName(admin, "TwinSpace")
            Log.d("TwinSpaceAdmin", "TwinSpace profile successfully provisioned and enabled")
        } catch (e: Exception) {
            Log.e("TwinSpaceAdmin", "Error enabling profile: ${e.message}", e)
        }
    }

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.d("TwinSpaceAdmin", "TwinSpace Admin Enabled")
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.d("TwinSpaceAdmin", "TwinSpace Admin Disabled")
    }

    companion object {
        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context.applicationContext, TwinDeviceAdminReceiver::class.java)
        }
    }
}
