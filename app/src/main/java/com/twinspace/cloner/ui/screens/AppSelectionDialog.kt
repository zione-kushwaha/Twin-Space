package com.twinspace.cloner.ui.screens

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.twinspace.cloner.core.AppItem
import com.twinspace.cloner.ui.components.drawableToBitmap
import com.twinspace.cloner.ui.theme.TwinBg
import com.twinspace.cloner.ui.theme.TwinBorder
import com.twinspace.cloner.ui.theme.TwinPrimary
import com.twinspace.cloner.ui.theme.TwinPrimarySoft
import com.twinspace.cloner.ui.theme.TwinSuccess
import com.twinspace.cloner.ui.theme.TwinSurface
import com.twinspace.cloner.ui.theme.TwinSurfaceAlt
import com.twinspace.cloner.ui.theme.TwinTextMuted
import com.twinspace.cloner.ui.theme.TwinTextPrimary
import com.twinspace.cloner.ui.theme.TwinTextSecondary

private val POPULAR_PACKAGES = setOf(
    "com.whatsapp",
    "org.telegram.messenger",
    "com.facebook.katana",
    "com.instagram.android",
    "com.facebook.orca",
    "com.zhiliaoapp.musically",
    "com.google.android.youtube",
    "com.twitter.android",
    "com.snapchat.android",
    "com.discord"
)

@Composable
fun AppSelectionDialog(
    installedApps: List<AppItem>,
    onCloneApp: (AppItem) -> Unit,
    onInstallStandalone: (AppItem) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var targetAppForChoice by remember { mutableStateOf<AppItem?>(null) }

    val context = LocalContext.current

    val recommendedApps = remember(installedApps) {
        installedApps.filter { app ->
            POPULAR_PACKAGES.contains(app.packageName) ||
                    app.appName.contains("WhatsApp", ignoreCase = true) ||
                    app.appName.contains("Telegram", ignoreCase = true) ||
                    app.appName.contains("Facebook", ignoreCase = true) ||
                    app.appName.contains("Instagram", ignoreCase = true) ||
                    app.appName.contains("TikTok", ignoreCase = true)
        }
    }

    val filteredApps = remember(searchQuery, installedApps) {
        if (searchQuery.isBlank()) installedApps
        else installedApps.filter {
            it.appName.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = TwinSurface),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .border(1.dp, TwinBorder, RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Add to TwinSpace",
                            color = TwinTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Select an app to clone into your isolated space",
                            color = TwinTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(TwinSurfaceAlt, CircleShape)
                            .border(1.dp, TwinBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TwinTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search installed apps...", color = TwinTextMuted, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TwinPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = TwinTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TwinPrimary,
                        unfocusedBorderColor = TwinBorder,
                        focusedContainerColor = TwinBg,
                        unfocusedContainerColor = TwinBg,
                        focusedTextColor = TwinTextPrimary,
                        unfocusedTextColor = TwinTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Recommended row (if searching is empty and recommended apps available)
                if (searchQuery.isBlank() && recommendedApps.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "POPULAR DUAL APPS",
                        color = TwinTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (app in recommendedApps) {
                            val isAlreadyCloned = app.isCloned
                            Column(
                                modifier = Modifier
                                    .width(76.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(TwinSurfaceAlt)
                                    .border(1.dp, if (isAlreadyCloned) TwinSuccess.copy(alpha = 0.5f) else TwinBorder, RoundedCornerShape(12.dp))
                                    .clickable {
                                        targetAppForChoice = app
                                    }
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (app.icon != null) {
                                    val bitmap = drawableToBitmap(app.icon)
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = app.appName,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = app.appName,
                                    color = TwinTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = if (isAlreadyCloned) "+ Clone #${app.cloneIndex + 1}" else "+ Clone",
                                    color = if (isAlreadyCloned) TwinSuccess else TwinPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "ALL APPS (${filteredApps.size})",
                    color = TwinTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                // App list
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        val isAlreadyCloned = app.isCloned
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TwinSurfaceAlt, RoundedCornerShape(12.dp))
                                .border(1.dp, if (isAlreadyCloned) TwinSuccess.copy(alpha = 0.35f) else TwinBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    targetAppForChoice = app
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (app.icon != null) {
                                val bitmap = drawableToBitmap(app.icon)
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = app.appName,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(TwinPrimarySoft, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = app.appName.take(1).uppercase(),
                                        color = TwinPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.appName,
                                    color = TwinTextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isAlreadyCloned) {
                                        "${app.packageName} • ${app.cloneIndex} clone${if (app.cloneIndex > 1) "s" else ""} in space"
                                    } else {
                                        app.packageName
                                    },
                                    color = if (isAlreadyCloned) TwinSuccess else TwinTextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isAlreadyCloned) TwinSuccess else TwinPrimary)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (isAlreadyCloned) "Clone #${app.cloneIndex + 1}" else "Clone",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal to choose Standalone System App vs TwinSpace Private Container
    targetAppForChoice?.let { choiceApp ->
        AlertDialog(
            onDismissRequest = { targetAppForChoice = null },
            containerColor = TwinBg,
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    choiceApp.icon?.let { icon ->
                        Image(
                            bitmap = drawableToBitmap(icon).asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Column {
                        Text(
                            text = "Clone ${choiceApp.appName}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TwinTextPrimary
                        )
                        Text(
                            text = "Select installation destination",
                            fontSize = 11.sp,
                            color = TwinTextSecondary
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Option 1: Standalone System App
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = TwinPrimary.copy(alpha = 0.08f)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, TwinPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val app = choiceApp
                                targetAppForChoice = null
                                onInstallStandalone(app)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(TwinPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Install Standalone App",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TwinPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(TwinPrimary, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "SYSTEM APP LIST",
                                            color = Color.White,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Installs directly into phone's system app drawer as a separate app. Also allows sharing/exporting the APK.",
                                    fontSize = 11.sp,
                                    color = TwinTextSecondary
                                )
                            }
                        }
                    }

                    // Option 2: TwinSpace Private Container
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = TwinBorder.copy(alpha = 0.2f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TwinBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val app = choiceApp
                                targetAppForChoice = null
                                onCloneApp(app)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(TwinTextSecondary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = TwinTextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Clone in TwinSpace",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TwinTextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Runs inside TwinSpace private sandbox without modifying phone system.",
                                    fontSize = 11.sp,
                                    color = TwinTextSecondary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { targetAppForChoice = null }) {
                    Text("Cancel", color = TwinTextSecondary)
                }
            }
        )
    }
}
