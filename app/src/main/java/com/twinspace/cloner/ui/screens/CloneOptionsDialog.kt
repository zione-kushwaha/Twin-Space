package com.twinspace.cloner.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DevicesOther
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.twinspace.cloner.core.AppItem
import com.twinspace.cloner.core.CloneSettings
import com.twinspace.cloner.ui.components.drawableToBitmap
import com.twinspace.cloner.ui.theme.AppClonerDeepTeal
import com.twinspace.cloner.ui.theme.AppClonerTeal
import com.twinspace.cloner.ui.theme.CardSubtle
import com.twinspace.cloner.ui.theme.CheckmarkTeal
import com.twinspace.cloner.ui.theme.TextDarkMuted
import com.twinspace.cloner.ui.theme.TextDarkPrimary
import com.twinspace.cloner.ui.theme.TextDarkSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloneOptionsDialog(
    app: AppItem,
    onDismiss: () -> Unit,
    onConfirmClone: (AppItem, CloneSettings) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Modding State
    var customName by remember { mutableStateOf(app.appName) }
    var cloneNumber by remember { mutableIntStateOf(1) }
    var hueShift by remember { mutableFloatStateOf(0f) }
    var iconRotation by remember { mutableFloatStateOf(0f) }
    var badgeText by remember { mutableStateOf("2") }

    // Privacy Options
    var passwordProtect by remember { mutableStateOf(false) }
    var secretPin by remember { mutableStateOf("1234") }
    var fakeCalcDisguise by remember { mutableStateOf(false) }
    var incognitoMode by remember { mutableStateOf(false) }
    var preventScreenshots by remember { mutableStateOf(true) }
    var stealthNotifications by remember { mutableStateOf(false) }

    // Identity & Tracking
    var randomizeAndroidId by remember { mutableStateOf(true) }
    var spoofWifiMac by remember { mutableStateOf(true) }
    var spoofBluetoothMac by remember { mutableStateOf(true) }
    var hideSimInfo by remember { mutableStateOf(true) }
    var randomizeBattery by remember { mutableStateOf(false) }

    // Mock Options
    var spoofLocation by remember { mutableStateOf(false) }
    var spoofTimeZone by remember { mutableStateOf(false) }

    // Display Options
    var forceDarkMode by remember { mutableStateOf(false) }
    var immersiveMode by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = AppClonerTeal,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Cloning Options",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDarkPrimary
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextDarkSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // App Preview Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        if (app.icon != null) {
                            val bitmap = drawableToBitmap(app.icon)
                            androidx.compose.foundation.Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = app.appName,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .rotate(iconRotation)
                            )
                        }
                        if (badgeText.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .background(AppClonerTeal, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (customName.isBlank()) app.appName else customName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextDarkPrimary
                        )
                        Text(
                            text = "Clone #$cloneNumber • ${app.packageName}",
                            fontSize = 12.sp,
                            color = TextDarkSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Scrollable Options List
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // SECTION 1: NAME & ICON OPTIONS
                SectionHeader(title = "Name & icon options", icon = Icons.Default.ColorLens)

                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("App Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppClonerTeal,
                        focusedLabelColor = AppClonerTeal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Clone number: $cloneNumber", color = TextDarkPrimary, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (i in 1..4) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (cloneNumber == i) AppClonerTeal else Color(0xFFE2E8F0))
                                    .clickable { cloneNumber = i },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$i",
                                    color = if (cloneNumber == i) Color.White else TextDarkPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Rotate icon: ${iconRotation.toInt()}°", color = TextDarkPrimary, fontSize = 14.sp)
                    IconButton(
                        onClick = {
                            iconRotation = (iconRotation + 90f) % 360f
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.RotateRight,
                            contentDescription = "Rotate",
                            tint = AppClonerTeal
                        )
                    }
                }

                OutlinedTextField(
                    value = badgeText,
                    onValueChange = { badgeText = it },
                    label = { Text("Badge Text (e.g. 2, Work, VIP)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppClonerTeal,
                        focusedLabelColor = AppClonerTeal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // SECTION 2: PRIVACY OPTIONS
                SectionHeader(title = "Privacy options", icon = Icons.Default.Security)

                OptionToggleRow(
                    title = "Password-protect clone",
                    subtitle = "Require PIN before opening the clone",
                    checked = passwordProtect,
                    onChecked = { passwordProtect = it }
                )

                if (passwordProtect) {
                    OutlinedTextField(
                        value = secretPin,
                        onValueChange = { if (it.length <= 6) secretPin = it },
                        label = { Text("Secret PIN") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppClonerTeal,
                            focusedLabelColor = AppClonerTeal
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OptionToggleRow(
                    title = "Fake Calculator Disguise",
                    subtitle = "Appears as a normal calculator until PIN is entered",
                    checked = fakeCalcDisguise,
                    onChecked = { fakeCalcDisguise = it }
                )

                OptionToggleRow(
                    title = "Prevent screenshots",
                    subtitle = "Blocks screenshots and screen recording (FLAG_SECURE)",
                    checked = preventScreenshots,
                    onChecked = { preventScreenshots = it }
                )

                OptionToggleRow(
                    title = "Incognito mode",
                    subtitle = "Wipe cache and sensitive storage when clone is closed",
                    checked = incognitoMode,
                    onChecked = { incognitoMode = it }
                )

                OptionToggleRow(
                    title = "Stealth notifications",
                    subtitle = "Hide sensitive notification contents and previews",
                    checked = stealthNotifications,
                    onChecked = { stealthNotifications = it }
                )

                // SECTION 3: IDENTITY & TRACKING OPTIONS
                SectionHeader(title = "Identity & tracking options", icon = Icons.Default.Fingerprint)

                OptionToggleRow(
                    title = "Randomize Android ID",
                    subtitle = "Generate spoofed Android ID to prevent device tracking",
                    checked = randomizeAndroidId,
                    onChecked = { randomizeAndroidId = it }
                )

                OptionToggleRow(
                    title = "Spoof Wi-Fi MAC address",
                    subtitle = "Provide randomized MAC address to cloned app",
                    checked = spoofWifiMac,
                    onChecked = { spoofWifiMac = it }
                )

                OptionToggleRow(
                    title = "Spoof Bluetooth MAC address",
                    subtitle = "Hide hardware Bluetooth identifier",
                    checked = spoofBluetoothMac,
                    onChecked = { spoofBluetoothMac = it }
                )

                OptionToggleRow(
                    title = "Hide SIM & network operator info",
                    subtitle = "Prevent app from reading carrier code or SIM serial",
                    checked = hideSimInfo,
                    onChecked = { hideSimInfo = it }
                )

                OptionToggleRow(
                    title = "Randomize battery level",
                    subtitle = "Mock battery status to disrupt battery fingerprinting",
                    checked = randomizeBattery,
                    onChecked = { randomizeBattery = it }
                )

                // SECTION 4: MOCK OPTIONS
                SectionHeader(title = "Mock options", icon = Icons.Default.LocationOn)

                OptionToggleRow(
                    title = "Spoof GPS location",
                    subtitle = "Mock latitude & longitude (San Francisco, Tokyo, London)",
                    checked = spoofLocation,
                    onChecked = { spoofLocation = it }
                )

                OptionToggleRow(
                    title = "Fake time zone",
                    subtitle = "Spoof system timezone independently from device",
                    checked = spoofTimeZone,
                    onChecked = { spoofTimeZone = it }
                )

                // SECTION 5: DISPLAY OPTIONS
                SectionHeader(title = "Display options", icon = Icons.Default.Tv)

                OptionToggleRow(
                    title = "Force Dark Mode",
                    subtitle = "Invert color palette for dark backgrounds",
                    checked = forceDarkMode,
                    onChecked = { forceDarkMode = it }
                )

                OptionToggleRow(
                    title = "Immersive full-screen mode",
                    subtitle = "Hide system status bar and navigation bar",
                    checked = immersiveMode,
                    onChecked = { immersiveMode = it }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Create Clone Button
            Button(
                onClick = {
                    val settings = CloneSettings(
                        customName = customName,
                        cloneNumber = cloneNumber,
                        hueRotation = hueShift,
                        iconRotation = iconRotation,
                        badgeText = badgeText,
                        isPasswordProtected = passwordProtect,
                        secretPin = secretPin,
                        isFakeCalculatorDisguise = fakeCalcDisguise,
                        isIncognitoMode = incognitoMode,
                        preventScreenshots = preventScreenshots,
                        stealthNotifications = stealthNotifications,
                        randomizeAndroidId = randomizeAndroidId,
                        spoofWifiMac = spoofWifiMac,
                        spoofBluetoothMac = spoofBluetoothMac,
                        hideSimAndCarrier = hideSimInfo,
                        randomizeBatteryLevel = randomizeBattery,
                        spoofLocation = spoofLocation,
                        spoofTimeZone = if (spoofTimeZone) "UTC" else "Auto",
                        forceDarkMode = forceDarkMode,
                        immersiveMode = immersiveMode
                    )
                    onConfirmClone(app, settings)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppClonerTeal,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Clone ${if (customName.isBlank()) app.appName else customName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppClonerTeal,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            color = AppClonerTeal,
            fontSize = 14.sp
        )
    }
}

@Composable
fun OptionToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChecked(!checked) }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextDarkPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            Text(
                text = subtitle,
                color = TextDarkSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = null, // handled by Row clickable to avoid event collision
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AppClonerTeal,
                uncheckedThumbColor = Color(0xFF94A3B8),
                uncheckedTrackColor = Color(0xFFE2E8F0)
            )
        )
    }
}
