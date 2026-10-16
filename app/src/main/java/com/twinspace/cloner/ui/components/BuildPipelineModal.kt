package com.twinspace.cloner.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.twinspace.cloner.domain.model.CloneTaskStatus
import com.twinspace.cloner.ui.theme.TwinBg
import com.twinspace.cloner.ui.theme.TwinBorder
import com.twinspace.cloner.ui.theme.TwinDanger
import com.twinspace.cloner.ui.theme.TwinPrimary
import com.twinspace.cloner.ui.theme.TwinPrimarySoft
import com.twinspace.cloner.ui.theme.TwinSuccess
import com.twinspace.cloner.ui.theme.TwinSurfaceAlt
import com.twinspace.cloner.ui.theme.TwinTextMuted
import com.twinspace.cloner.ui.theme.TwinTextPrimary
import com.twinspace.cloner.ui.theme.TwinTextSecondary
import java.io.File

@Composable
fun BuildPipelineModal(
    status: CloneTaskStatus,
    targetAppName: String,
    targetAppIcon: Drawable?,
    onDismiss: () -> Unit,
    onInstallApk: (File) -> Unit,
    onShareApk: (File, String) -> Unit
) {
    if (status is CloneTaskStatus.Idle) return

    val isSuccess = status is CloneTaskStatus.Success
    val isError = status is CloneTaskStatus.Error
    val inProgress = status as? CloneTaskStatus.InProgress

    AlertDialog(
        onDismissRequest = {
            if (isSuccess || isError) onDismiss()
        },
        containerColor = TwinBg,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                targetAppIcon?.let { icon ->
                    Image(
                        bitmap = drawableToBitmap(icon).asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Column {
                    Text(
                        text = if (isSuccess) "Standalone Clone Ready!" else "Cloning $targetAppName",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TwinTextPrimary
                    )
                    Text(
                        text = if (isSuccess) "System App Ready" else "Building Standalone APK",
                        fontSize = 11.sp,
                        color = TwinTextSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                when {
                    isError -> {
                        val errorStatus = status as CloneTaskStatus.Error
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = TwinDanger,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorStatus.message,
                            color = TwinDanger,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    inProgress != null -> {
                        // 4-Step Animated Pipeline Tracker
                        val curStep = inProgress.step
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BuildStepIndicator(
                                title = "Extract",
                                stepIndex = 1,
                                isCompleted = curStep.stepIndex > 1,
                                isActive = curStep == CloneTaskStatus.PipelineStep.EXTRACTING
                            )
                            BuildStepLine(isCompleted = curStep.stepIndex > 1)
                            BuildStepIndicator(
                                title = "Rewrite",
                                stepIndex = 2,
                                isCompleted = curStep.stepIndex > 2,
                                isActive = curStep == CloneTaskStatus.PipelineStep.REWRITING
                            )
                            BuildStepLine(isCompleted = curStep.stepIndex > 2)
                            BuildStepIndicator(
                                title = "Sign v1-v3",
                                stepIndex = 3,
                                isCompleted = curStep.stepIndex > 3,
                                isActive = curStep == CloneTaskStatus.PipelineStep.SIGNING
                            )
                            BuildStepLine(isCompleted = curStep.stepIndex > 3)
                            BuildStepIndicator(
                                title = "Ready",
                                stepIndex = 4,
                                isCompleted = curStep == CloneTaskStatus.PipelineStep.COMPLETED,
                                isActive = curStep == CloneTaskStatus.PipelineStep.COMPLETED
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TwinPrimarySoft, RoundedCornerShape(10.dp))
                                .border(1.dp, TwinPrimary.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                                .padding(vertical = 8.dp, horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = inProgress.message,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TwinPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    isSuccess -> {
                        val successStatus = status as CloneTaskStatus.Success
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = TwinSuccess,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "APK generated & signed with v1+v2+v3!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TwinSuccess
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "You can install it directly into your phone's app list or share the APK.",
                            fontSize = 11.sp,
                            color = TwinTextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Action 1: Install Now
                        Button(
                            onClick = { onInstallApk(successStatus.apkFile) },
                            colors = ButtonDefaults.buttonColors(containerColor = TwinPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Install to System (App Drawer)", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Action 2: Share APK
                        Button(
                            onClick = { onShareApk(successStatus.apkFile, successStatus.appName) },
                            colors = ButtonDefaults.buttonColors(containerColor = TwinSurfaceAlt),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TwinBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = TwinTextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Share APK File with Others", color = TwinTextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isSuccess || isError) {
                TextButton(onClick = onDismiss) {
                    Text("Done", color = TwinPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@Composable
private fun BuildStepIndicator(
    title: String,
    stepIndex: Int,
    isCompleted: Boolean,
    isActive: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> TwinSuccess
                        isActive -> TwinPrimary
                        else -> TwinSurfaceAlt
                    }
                )
                .border(
                    1.dp,
                    when {
                        isCompleted -> TwinSuccess
                        isActive -> TwinPrimary
                        else -> TwinBorder
                    },
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            } else {
                Text(
                    text = "$stepIndex",
                    color = if (isActive) Color.White else TwinTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = if (isActive || isCompleted) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive || isCompleted) TwinTextPrimary else TwinTextMuted
        )
    }
}

@Composable
private fun BuildStepLine(isCompleted: Boolean) {
    Box(
        modifier = Modifier
            .width(24.dp)
            .height(2.dp)
            .background(if (isCompleted) TwinSuccess else TwinBorder)
    )
}
