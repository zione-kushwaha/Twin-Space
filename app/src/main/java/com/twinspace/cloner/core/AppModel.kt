package com.twinspace.cloner.core

import android.graphics.drawable.Drawable
import android.os.UserHandle

data class CloneSettings(
    val customName: String = "",
    val cloneNumber: Int = 1,
    val hueRotation: Float = 0f, // 0 to 360 degrees
    val iconRotation: Float = 0f, // 0, 90, 180, 270
    val badgeText: String = "",
    val isPasswordProtected: Boolean = false,
    val secretPin: String = "",
    val isFakeCalculatorDisguise: Boolean = false,
    val isIncognitoMode: Boolean = false,
    val preventScreenshots: Boolean = false,
    val stealthNotifications: Boolean = false,
    // Identity & Spoofing
    val randomizeAndroidId: Boolean = true,
    val spoofAndroidIdValue: String = "",
    val spoofWifiMac: Boolean = true,
    val spoofBluetoothMac: Boolean = true,
    val hideSimAndCarrier: Boolean = true,
    val randomizeBatteryLevel: Boolean = false,
    val customUserAgent: String = "",
    // Mock Options
    val spoofLocation: Boolean = false,
    val latitude: Double = 37.7749,
    val longitude: Double = -122.4194,
    val spoofTimeZone: String = "Auto",
    // Display & Launching
    val forceDarkMode: Boolean = false,
    val immersiveMode: Boolean = false,
    val rotationLock: String = "None", // None, Portrait, Landscape
    val autoStartOnLaunch: Boolean = false,
    val confirmExit: Boolean = false
)

data class AppItem(
    val packageName: String,
    val appName: String,
    val icon: Drawable? = null,
    val isCloned: Boolean = false,
    val isFrozen: Boolean = false,
    val userHandle: UserHandle? = null,
    val cloneIndex: Int = 1,
    val settings: CloneSettings = CloneSettings()
)

data class WebTwinService(
    val id: String,
    val name: String,
    val url: String,
    val colorHex: Long,
    val description: String
)

enum class ScreenTab {
    CLONED_APPS,
    STANDALONE_APKS,
    WEB_TWINS,
    SETTINGS
}
