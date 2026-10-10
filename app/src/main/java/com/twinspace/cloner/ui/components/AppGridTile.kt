package com.twinspace.cloner.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.twinspace.cloner.core.AppItem
import com.twinspace.cloner.ui.theme.TwinBorder
import com.twinspace.cloner.ui.theme.TwinPrimary
import com.twinspace.cloner.ui.theme.TwinPrimarySoft
import com.twinspace.cloner.ui.theme.TwinSurface
import com.twinspace.cloner.ui.theme.TwinTextPrimary

/**
 * Ultra-Premium Cloned App Tile in the TwinSpace launcher grid.
 * Features glassmorphism card, glowing corner multi-instance badge, and clean micro-tags.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppGridTile(
    app: AppItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDefault = app.cloneIndex == 0 || !app.isCloned
    val cloneNumber = if (isDefault) {
        "1"
    } else if (app.settings.badgeText.isNotBlank()) {
        app.settings.badgeText
    } else {
        (app.cloneIndex + 1).toString()
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Icon Box with High-End Glowing Badge
        Box(
            modifier = Modifier.size(70.dp),
            contentAlignment = Alignment.Center
        ) {
            // Icon Card Container with subtle frosted shadow & gradient border
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(18.dp),
                        spotColor = if (isDefault) Color(0x334F46E5) else Color(0x3306B6D4),
                        ambientColor = Color(0x18000000)
                    )
                    .clip(RoundedCornerShape(18.dp))
                    .background(TwinSurface)
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(Color.White, if (isDefault) Color(0x444F46E5) else Color(0x4406B6D4))
                        ),
                        shape = RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (app.icon != null) {
                    val bitmap = drawableToBitmap(app.icon)
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = app.appName,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .rotate(app.settings.iconRotation)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                Brush.linearGradient(listOf(TwinPrimarySoft, Color(0xFFEEF2FF))),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = app.appName.take(1).uppercase(),
                            color = TwinPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp
                        )
                    }
                }
            }

            // High-End Multi-Instance Badge positioned at top-end with vivid gradient & white outline
            val badgeGradient = when {
                isDefault -> Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF6366F1)))
                app.cloneIndex == 1 -> Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
                app.cloneIndex == 2 -> Brush.linearGradient(listOf(Color(0xFF06B6D4), Color(0xFF0284C7)))
                else -> Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFFD946EF)))
            }

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.TopEnd)
                    .shadow(4.dp, CircleShape, spotColor = if (isDefault) Color(0x664F46E5) else Color(0x6606B6D4))
                    .clip(CircleShape)
                    .background(badgeGradient)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = cloneNumber.take(3),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.height(7.dp))

        // App Label
        Text(
            text = if (app.settings.customName.isNotBlank()) app.settings.customName else app.appName,
            color = TwinTextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )

        // Clear micro-tag for Default App vs Clone #1, Clone #2, Clone #3...
        Spacer(modifier = Modifier.height(2.dp))
        if (isDefault) {
            Box(
                modifier = Modifier
                    .background(Color(0xFFEEF2FF), RoundedCornerShape(4.dp))
                    .border(0.5.dp, Color(0xFFC7D2FE), RoundedCornerShape(4.dp))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "DEFAULT",
                    color = Color(0xFF4F46E5),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else if (app.cloneIndex == 1) {
            Box(
                modifier = Modifier
                    .background(Color(0xFFF0FDF4), RoundedCornerShape(4.dp))
                    .border(0.5.dp, Color(0xFFBBF7D0), RoundedCornerShape(4.dp))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "CLONE #1",
                    color = Color(0xFF16A34A),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .background(Color(0xFFECFDF5), RoundedCornerShape(4.dp))
                    .border(0.5.dp, Color(0xFFA7F3D0), RoundedCornerShape(4.dp))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "CLONE #${app.cloneIndex}",
                    color = Color(0xFF059669),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Parallel Space style '+' Add App Tile in the launcher grid with gradient frosted glass accent.
 */
@Composable
fun AddAppGridTile(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(70.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .shadow(4.dp, RoundedCornerShape(18.dp), spotColor = Color(0x18000000))
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(listOf(Color.White, Color(0xFFF8FAFC)))
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(listOf(Color(0xFF818CF8), Color(0xFF38BDF8))),
                        shape = RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Clone",
                    tint = TwinPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = "Add Clone",
            color = TwinPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}
