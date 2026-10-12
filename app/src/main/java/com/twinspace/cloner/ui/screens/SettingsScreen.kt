package com.twinspace.cloner.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.twinspace.cloner.R
import com.twinspace.cloner.core.PreferencesManager
import com.twinspace.cloner.ui.theme.TwinBg
import com.twinspace.cloner.ui.theme.TwinBorder
import com.twinspace.cloner.ui.theme.TwinDanger
import com.twinspace.cloner.ui.theme.TwinPrimary
import com.twinspace.cloner.ui.theme.TwinPrimaryDark
import com.twinspace.cloner.ui.theme.TwinPrimarySoft
import com.twinspace.cloner.ui.theme.TwinSuccess
import com.twinspace.cloner.ui.theme.TwinSuccessSoft
import com.twinspace.cloner.ui.theme.TwinSurface
import com.twinspace.cloner.ui.theme.TwinSurfaceAlt
import com.twinspace.cloner.ui.theme.TwinTextPrimary
import com.twinspace.cloner.ui.theme.TwinTextSecondary

@Composable
fun SettingsScreen(
    prefs: PreferencesManager,
    onLockApp: () -> Unit,
    onShowOnboarding: () -> Unit = {},
    onOpenStorageOptimizer: () -> Unit = {},
    onOpenTrustCenter: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var disguiseMode by remember { mutableStateOf(prefs.isDisguiseModeEnabled) }
    var appLock by remember { mutableStateOf(prefs.isAppLockEnabled) }
    var stealthNotifs by remember { mutableStateOf(prefs.isStealthNotificationEnabled) }
    var showPinDialog by remember { mutableStateOf(false) }
    var newPinText by remember { mutableStateOf("") }
    var showDisguiseConfirmDialog by remember { mutableStateOf(false) }
    var disguiseStep by remember { mutableIntStateOf(1) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TwinBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_twinspace_brand),
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "TwinSpace Settings & Security",
                    color = TwinTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Configure privacy, sandbox isolation, and PIN disguise",
                    color = TwinTextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Section: Stealth & Privacy
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = TwinSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, TwinBorder, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "STEALTH & DISGUISE",
                    color = TwinPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                SettingSwitchRow(
                    painter = painterResource(id = R.drawable.ic_calculate_brand),
                    title = "Calculator Disguise",
                    subtitle = "App appears as a working calculator until secret PIN is entered",
                    checked = disguiseMode,
                    onCheckedChange = { enable ->
                        if (enable) {
                            disguiseStep = 1
                            showDisguiseConfirmDialog = true
                        } else {
                            disguiseMode = false
                            prefs.isDisguiseModeEnabled = false
                            Toast.makeText(context, "Calculator Disguise disabled", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SettingClickRow(
                    painter = rememberVectorPainter(Icons.Default.Key),
                    title = "Change Secret PIN",
                    subtitle = "Current PIN: ${prefs.secretPin}",
                    onClick = {
                        newPinText = prefs.secretPin
                        showPinDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SettingClickRow(
                    painter = rememberVectorPainter(Icons.Default.Info),
                    title = "Default Password & Tutorial",
                    subtitle = "Default PIN is ${prefs.secretPin} • View guide on how to unlock",
                    onClick = onShowOnboarding
                )

                Spacer(modifier = Modifier.height(16.dp))

                SettingSwitchRow(
                    painter = painterResource(id = R.drawable.ic_lock_brand),
                    title = "Lock TwinSpace Session",
                    subtitle = "Require PIN or biometric authentication every time app opens",
                    checked = appLock,
                    onCheckedChange = {
                        appLock = it
                        prefs.isAppLockEnabled = it
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SettingSwitchRow(
                    painter = rememberVectorPainter(Icons.Default.NotificationsOff),
                    title = "Stealth Notifications",
                    subtitle = "Hide sender and message details for cloned accounts",
                    checked = stealthNotifs,
                    onCheckedChange = {
                        stealthNotifs = it
                        prefs.isStealthNotificationEnabled = it
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Storage & Maintenance
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = TwinSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, TwinBorder, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "STORAGE & ENGINE",
                    color = TwinPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                SettingClickRow(
                    painter = rememberVectorPainter(Icons.Default.CleaningServices),
                    title = "Storage & Cache Optimizer",
                    subtitle = "Analyze storage usage and free temporary clone cache",
                    onClick = onOpenStorageOptimizer
                )

                Spacer(modifier = Modifier.height(16.dp))

                SettingClickRow(
                    painter = painterResource(id = R.drawable.ic_shield_brand),
                    title = "Privacy & Trust Center",
                    subtitle = "Review 100% on-device sandboxing & zero data tracking policy",
                    onClick = onOpenTrustCenter
                )

                Spacer(modifier = Modifier.height(16.dp))

                SettingClickRow(
                    painter = rememberVectorPainter(Icons.Default.Lock),
                    title = "Lock Session Now",
                    subtitle = "Instantly secure and lock TwinSpace dashboard",
                    onClick = onLockApp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // About TwinSpace
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = TwinSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, TwinBorder, RoundedCornerShape(12.dp))
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_twinspace_brand),
                    contentDescription = null,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "TwinSpace Pro (v4.0.8)",
                        color = TwinTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Dual Space & Cloner • Light Edition",
                        color = TwinTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // Strict 2-Step Password Confirmation Dialog for Calculator Disguise
    if (showDisguiseConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                showDisguiseConfirmDialog = false
                disguiseMode = false
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(TwinPrimarySoft, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_calculate_brand),
                            contentDescription = null,
                            tint = TwinPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (disguiseStep == 1) "Enable Disguise (Step 1 of 2)" else "Confirm Passcode (Step 2 of 2)",
                        color = TwinTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column {
                    if (disguiseStep == 1) {
                        Text(
                            text = "TwinSpace will mask its launcher and interface as a standard calculator.",
                            color = TwinTextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TwinPrimarySoft, RoundedCornerShape(10.dp))
                                .border(1.dp, TwinPrimary.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "YOUR SECRET UNLOCK PASSCODE",
                                    color = TwinPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = prefs.secretPin,
                                    color = TwinPrimaryDark,
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 5.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Enter ${prefs.secretPin} and tap '=' to unlock",
                                    color = TwinTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Please memorize this passcode before moving to confirmation.",
                            color = TwinTextSecondary,
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            text = "Strict Safety Verification: Please confirm that you have strictly memorized the secret passcode.",
                            color = TwinTextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TwinSuccessSoft, RoundedCornerShape(10.dp))
                                .border(1.dp, TwinSuccess.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "CONFIRM PASSCODE (2nd Verification)",
                                    color = TwinSuccess,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = prefs.secretPin,
                                    color = TwinSuccess,
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 5.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Formula: ${prefs.secretPin} =",
                                    color = TwinTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Important: If you forget this PIN, you will not be able to access your private spaces.",
                            color = TwinDanger,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                if (disguiseStep == 1) {
                    Button(
                        onClick = { disguiseStep = 2 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TwinPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Next: Confirm Passcode (2/2)", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            prefs.isDisguiseModeEnabled = true
                            disguiseMode = true
                            showDisguiseConfirmDialog = false
                            Toast.makeText(
                                context,
                                "Calculator Disguise enabled! Enter ${prefs.secretPin} = to unlock",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TwinSuccess,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("I Memorized ${prefs.secretPin} • Enable", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDisguiseConfirmDialog = false
                        disguiseMode = false
                    }
                ) {
                    Text("Cancel", color = TwinTextSecondary)
                }
            },
            containerColor = TwinSurface,
            shape = RoundedCornerShape(12.dp)
        )
    }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = {
                Text(
                    text = "Set Secret PIN",
                    color = TwinTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter a 4 to 6 digit PIN to unlock Calculator Disguise and TwinSpace:",
                        color = TwinTextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPinText,
                        onValueChange = { if (it.length <= 6) newPinText = it },
                        placeholder = { Text("e.g. 1234") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TwinPrimary,
                            unfocusedBorderColor = TwinBorder,
                            focusedTextColor = TwinTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPinText.length >= 4) {
                            prefs.secretPin = newPinText
                            showPinDialog = false
                            Toast.makeText(context, "PIN updated to $newPinText", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "PIN must be at least 4 digits", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TwinPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancel", color = TwinTextSecondary)
                }
            },
            containerColor = TwinSurface,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun SettingSwitchRow(
    painter: Painter,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(TwinPrimarySoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painter,
                contentDescription = null,
                tint = TwinPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TwinTextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TwinTextSecondary,
                fontSize = 11.5.sp,
                lineHeight = 15.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = TwinPrimary,
                uncheckedThumbColor = Color(0xFF94A3B8),
                uncheckedTrackColor = Color(0xFFE2E8F0)
            )
        )
    }
}

@Composable
private fun SettingClickRow(
    painter: Painter,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(TwinPrimarySoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painter,
                contentDescription = null,
                tint = TwinPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TwinTextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TwinTextSecondary,
                fontSize = 11.5.sp,
                lineHeight = 15.sp
            )
        }
    }
}
