package com.twinspace.cloner.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shortcut
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.twinspace.cloner.core.AppItem
import com.twinspace.cloner.ui.theme.TwinBorder
import com.twinspace.cloner.ui.theme.TwinCyan
import com.twinspace.cloner.ui.theme.TwinDanger
import com.twinspace.cloner.ui.theme.TwinDangerSoft
import com.twinspace.cloner.ui.theme.TwinPrimary
import com.twinspace.cloner.ui.theme.TwinPrimaryDark
import com.twinspace.cloner.ui.theme.TwinPrimarySoft
import com.twinspace.cloner.ui.theme.TwinSurface
import com.twinspace.cloner.ui.theme.TwinSurfaceAlt
import com.twinspace.cloner.ui.theme.TwinTextMuted
import com.twinspace.cloner.ui.theme.TwinTextPrimary
import com.twinspace.cloner.ui.theme.TwinTextSecondary

@Composable
fun AppItemCard(
    app: AppItem,
    onLaunch: () -> Unit,
    onShortcut: () -> Unit,
    onRemove: () -> Unit,
    onCustomize: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TwinSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, TwinBorder, RoundedCornerShape(12.dp))
            .clickable { onLaunch() }
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon with TwinSpace Badge
            Box(
                modifier = Modifier.size(52.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                if (app.icon != null) {
                    val bitmap = drawableToBitmap(app.icon)
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = app.appName,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .rotate(app.settings.iconRotation)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(TwinPrimarySoft, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = app.appName.take(1).uppercase(),
                            color = TwinPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }

                // Dual Badge
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(TwinPrimary, CircleShape)
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (app.settings.badgeText.isNotBlank()) app.settings.badgeText.take(2) else "2",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (app.settings.customName.isNotBlank()) app.settings.customName else app.appName,
                    color = TwinTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(TwinPrimarySoft, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Twin #${app.settings.cloneNumber}",
                            color = TwinPrimaryDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Isolated",
                        color = TwinTextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onCustomize != null) {
                    IconButton(
                        onClick = onCustomize,
                        modifier = Modifier
                            .size(32.dp)
                            .background(TwinSurfaceAlt, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Options",
                            tint = TwinTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Pin Shortcut button
                IconButton(
                    onClick = onShortcut,
                    modifier = Modifier
                        .size(32.dp)
                        .background(TwinSurfaceAlt, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shortcut,
                        contentDescription = "Add Shortcut",
                        tint = TwinPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Launch button
                IconButton(
                    onClick = onLaunch,
                    modifier = Modifier
                        .size(34.dp)
                        .background(TwinPrimary, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Launch",
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Delete button
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .size(32.dp)
                        .background(TwinDangerSoft, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Remove",
                        tint = TwinDanger,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

fun drawableToBitmap(drawable: Drawable): Bitmap {
    val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
    val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}