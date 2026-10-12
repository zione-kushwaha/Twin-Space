package com.twinspace.cloner.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.DoubleArrow
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.twinspace.cloner.ui.theme.AppClonerPageBg
import com.twinspace.cloner.ui.theme.AppClonerTeal
import com.twinspace.cloner.ui.theme.BadgeBlueBg
import com.twinspace.cloner.ui.theme.BadgeBlueIcon
import com.twinspace.cloner.ui.theme.BadgeGreenBg
import com.twinspace.cloner.ui.theme.BadgeGreenIcon
import com.twinspace.cloner.ui.theme.BadgeMagentaBg
import com.twinspace.cloner.ui.theme.BadgeMagentaIcon
import com.twinspace.cloner.ui.theme.BadgeOrangeBg
import com.twinspace.cloner.ui.theme.BadgeOrangeIcon
import com.twinspace.cloner.ui.theme.BadgeRedBg
import com.twinspace.cloner.ui.theme.BadgeRedIcon
import com.twinspace.cloner.ui.theme.BadgeYellowBg
import com.twinspace.cloner.ui.theme.BadgeYellowIcon
import com.twinspace.cloner.ui.theme.CheckmarkTeal
import com.twinspace.cloner.ui.theme.DateGreen
import com.twinspace.cloner.ui.theme.TextDarkMuted
import com.twinspace.cloner.ui.theme.TextDarkPrimary
import com.twinspace.cloner.ui.theme.TextDarkSecondary

@Composable
fun AddonsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppClonerPageBg)
    ) {
        // AppListo Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "App Cloner Premium & Add-ons",
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold
            )

            Box {
                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = Color.White
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Restore purchases") },
                        onClick = {
                            showMenu = false
                            Toast.makeText(context, "Purchases verified & up to date!", Toast.LENGTH_SHORT).show()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Usage & limits") },
                        onClick = {
                            showMenu = false
                            Toast.makeText(context, "Orange Package: 100 clones enabled across 5 devices", Toast.LENGTH_LONG).show()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("About App Cloner") },
                        onClick = {
                            showMenu = false
                            Toast.makeText(context, "App Cloner 4.0.8 by AppListo (TwinSpace Edition)", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        // Scrollable Cards List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // CARD 1: PREMIUM
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "Premium",
                            color = AppClonerTeal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "10 clones, 100 apps, 5 devices",
                            color = TextDarkPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "App Cloner Premium includes all premium options / features and 10 clones. The purchase is valid for 1, 3, 6 months, or a full year and does not automatically renew.",
                            color = TextDarkSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(18.dp))

                        // Item 1: Premium subscription
                        AddonItemRow(
                            icon = Icons.Default.Verified,
                            iconBg = Color(0xFFE0F2FE),
                            iconTint = Color(0xFF0284C7),
                            title = "Premium subscription",
                            description = "Choose or renew your Premium subscription.",
                            date = null,
                            isChecked = false,
                            onClick = {
                                Toast.makeText(context, "Premium is currently active!", Toast.LENGTH_SHORT).show()
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Item 2: Active 3 months
                        AddonItemRow(
                            icon = Icons.Default.Verified,
                            iconBg = Color(0xFFE0F2FE),
                            iconTint = Color(0xFF0284C7),
                            title = "Premium subscription (3 months)",
                            description = "",
                            date = "Aug 17, 2026",
                            isChecked = true,
                            onClick = {
                                Toast.makeText(context, "Valid until August 17, 2026", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            // CARD 2: ADD-ONS
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "Add-ons",
                            color = AppClonerTeal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add-ons extend the App Cloner functionality.\n\nAdd-ons are yearly purchases. Add-on purchases before June 2024 remain lifetime.",
                            color = TextDarkSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(18.dp))

                        // 1. Google Maps support
                        AddonItemRow(
                            icon = Icons.Default.LocationOn,
                            iconBg = Color(0xFFE2E8F0),
                            iconTint = Color(0xFF475569),
                            title = "Google Maps support",
                            description = "Enables Google Maps support in clones.",
                            date = "Aug 21, 2020",
                            isChecked = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 2. Safe update
                        AddonItemRow(
                            icon = Icons.Default.Security,
                            iconBg = Color(0xFFE2E8F0),
                            iconTint = Color(0xFF475569),
                            title = "Safe update",
                            description = "Safe update creates clones in a way which allows downgrading to the previous version without uninstalling the clone. The previous version is backed up automatically.",
                            date = "Aug 21, 2020",
                            isChecked = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3. Auto install
                        AddonItemRow(
                            icon = Icons.Default.Archive,
                            iconBg = Color(0xFFE2E8F0),
                            iconTint = Color(0xFF475569),
                            title = "Auto install",
                            description = "Automate app installations through an accessibility service, wireless debugging or root access.",
                            date = null,
                            isChecked = false
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 4. Export & import app data
                        AddonItemRow(
                            icon = Icons.Default.SwapVert,
                            iconBg = Color(0xFFE2E8F0),
                            iconTint = Color(0xFF475569),
                            title = "Export & import app data",
                            description = "Exporting and importing app data of cloned apps allows you to back up and restore internal app settings and files.",
                            date = null,
                            isChecked = false
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 5. Manifest & resource options
                        AddonItemRow(
                            icon = Icons.Default.Code,
                            iconBg = Color(0xFFE2E8F0),
                            iconTint = Color(0xFF475569),
                            title = "Manifest & resource options",
                            description = "Manifest & XML resource editors, app translator, etc.",
                            date = "Aug 21, 2020",
                            isChecked = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 6. Runtime modding options
                        AddonItemRow(
                            icon = Icons.Default.DoubleArrow,
                            iconBg = Color(0xFFE2E8F0),
                            iconTint = Color(0xFF475569),
                            title = "Runtime modding options",
                            description = "Enables the 'Send settings to clone' menu item to send modified runtime options to installed clones.",
                            date = "Aug 19, 2026",
                            isChecked = true
                        )
                    }
                }
            }

            // CARD 3: PACKAGES
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "Packages",
                            color = AppClonerTeal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Get one or more packages for additional features & more clones.\n\nAll packages require premium. Higher packages do not automatically include lower packages. Additional clones are not added to the clones of the premium version or other packages.",
                            color = TextDarkSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(18.dp))

                        // Blue package
                        PackageItemRow(
                            icon = Icons.Default.Diamond,
                            iconBg = BadgeBlueBg,
                            iconTint = BadgeBlueIcon,
                            title = "Blue package:",
                            bullet1 = "20 clones, 200 apps, 5 devices",
                            bullet2 = "Additional features",
                            date = null,
                            isChecked = false
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Green package
                        PackageItemRow(
                            icon = Icons.Default.Navigation,
                            iconBg = BadgeGreenBg,
                            iconTint = BadgeGreenIcon,
                            title = "Green package:",
                            bullet1 = "30 clones, 300 apps, 5 devices",
                            bullet2 = "Additional features",
                            date = null,
                            isChecked = false
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Yellow package
                        PackageItemRow(
                            icon = Icons.Default.Star,
                            iconBg = BadgeYellowBg,
                            iconTint = BadgeYellowIcon,
                            title = "Yellow package:",
                            bullet1 = "50 clones, 500 apps, 5 devices",
                            bullet2 = "Additional features",
                            date = null,
                            isChecked = false
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Orange package (ACTIVE in screenshot!)
                        PackageItemRow(
                            icon = Icons.Default.Star,
                            iconBg = BadgeOrangeBg,
                            iconTint = BadgeOrangeIcon,
                            title = "Orange package:",
                            bullet1 = "100 clones, 1000 apps, 5 devices",
                            bullet2 = "Additional features",
                            date = "Nov 27, 2019",
                            isChecked = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Red package
                        PackageItemRow(
                            icon = Icons.Default.Star,
                            iconBg = BadgeRedBg,
                            iconTint = BadgeRedIcon,
                            title = "Red package:",
                            bullet1 = "300 clones, 3000 apps, 5 devices",
                            bullet2 = "Additional features",
                            date = null,
                            isChecked = false
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Magenta package
                        PackageItemRow(
                            icon = Icons.Default.Star,
                            iconBg = BadgeMagentaBg,
                            iconTint = BadgeMagentaIcon,
                            title = "Magenta package:",
                            bullet1 = "500 clones, 5000 apps, 5 devices",
                            bullet2 = "Additional features",
                            date = null,
                            isChecked = false
                        )
                    }
                }
            }

            // CARD 4: EXTENSIONS
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "Extensions",
                            color = AppClonerTeal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Extensions further expand App Cloner's capabilities. They are monthly purchases and not included with any Ultra edition.",
                            color = TextDarkSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(18.dp))

                        // Proxying suite
                        AddonItemRow(
                            icon = Icons.Default.Key,
                            iconBg = Color(0xFFF1E5E5),
                            iconTint = Color(0xFF8B5E5E),
                            title = "Proxying suite",
                            description = "Provides options for using SOCKS, SOCKS VPN and WebView proxies and for trusting SSL certificates.",
                            date = "Aug 17, 2026",
                            isChecked = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Capturing suite
                        AddonItemRow(
                            icon = Icons.Default.CameraAlt,
                            iconBg = Color(0xFFF1E5E5),
                            iconTint = Color(0xFF8B5E5E),
                            title = "Capturing suite",
                            description = "Provides options to allow screenshots and screen recording as well as for video, audio and WebRTC recording.",
                            date = "Aug 18, 2026",
                            isChecked = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Monitoring suite
                        AddonItemRow(
                            icon = Icons.Default.Policy,
                            iconBg = Color(0xFFF1E5E5),
                            iconTint = Color(0xFF8B5E5E),
                            title = "Monitoring suite",
                            description = "Provides various monitoring options to help you uncover an app's internal behavior. A must-have for curious minds!",
                            date = null,
                            isChecked = false
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Remote Control
                        AddonItemRow(
                            icon = Icons.Default.Radio,
                            iconBg = Color(0xFFF1E5E5),
                            iconTint = Color(0xFF8B5E5E),
                            title = "Remote Control",
                            description = "Enables web-based remote control for multiple clones, with support for clone linking and event mirroring, clipboard sync and file uploads. Tap to learn more.",
                            date = null,
                            isChecked = false
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun AddonItemRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    description: String,
    date: String?,
    isChecked: Boolean,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.Top
    ) {
        // Icon Circle
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextDarkPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            if (description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
                    color = TextDarkSecondary,
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp
                )
            }
            if (date != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = DateGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = date,
                        color = DateGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (isChecked) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Active",
                tint = CheckmarkTeal,
                modifier = Modifier
                    .size(22.dp)
                    .align(Alignment.CenterVertically)
            )
        }
    }
}

@Composable
fun PackageItemRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    bullet1: String,
    bullet2: String,
    date: String?,
    isChecked: Boolean,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextDarkPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "• $bullet1",
                color = TextDarkPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )
            Text(
                text = "• $bullet2",
                color = TextDarkSecondary,
                fontSize = 13.sp
            )
            if (date != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = DateGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = date,
                        color = DateGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (isChecked) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Active",
                tint = CheckmarkTeal,
                modifier = Modifier
                    .size(22.dp)
                    .align(Alignment.CenterVertically)
            )
        }
    }
}
