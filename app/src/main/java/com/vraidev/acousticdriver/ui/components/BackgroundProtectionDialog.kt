package com.vraidev.acousticdriver.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vraidev.acousticdriver.ui.theme.*

@Composable
fun BackgroundProtectionDialog(
    isNotificationGranted: Boolean,
    onRequestNotification: () -> Unit,
    isBatteryIgnored: Boolean,
    onRequestBatteryOptimization: () -> Unit,
    onToggleBatteryOptimizationDone: (Boolean) -> Unit,
    onRecheckStatus: () -> Unit,
    isLockInRecentsDone: Boolean,
    onToggleLockInRecents: (Boolean) -> Unit,
    onOpenAppSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val pendingCount = (if (!isNotificationGranted) 1 else 0) +
            (if (!isBatteryIgnored) 1 else 0) +
            (if (!isLockInRecentsDone) 1 else 0)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SurfaceDark,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "BACKGROUND PERSISTENCE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Keep Audio Engine Running",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = onRecheckStatus,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Re-check Status",
                                tint = CyberCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (pendingCount == 0) SuccessGreen.copy(alpha = 0.2f) else AmberWarning.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (pendingCount == 0) "ALL SET ✓" else "$pendingCount PENDING",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (pendingCount == 0) SuccessGreen else AmberWarning
                            )
                        }
                    }
                }

                Text(
                    text = "Android and phone manufacturers (Samsung, Xiaomi HyperOS, Oppo) aggressively stop audio DSP when apps are in the background or swiped away. Complete these 3 steps for 24/7 background audio tuning:",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 14.dp)
                )

                // Scrollable Steps List
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Step 1: Notifications
                    SetupStepCard(
                        stepNumber = "1",
                        title = "Foreground Notification",
                        description = "Permits ongoing audio engine status. Keeps the DSP service active in memory during background music playback.",
                        isCompleted = isNotificationGranted,
                        actionLabel = "Allow Notifications",
                        onAction = onRequestNotification
                    )

                    // Step 2: Battery Optimization
                    SetupStepCard(
                        stepNumber = "2",
                        title = "Unrestricted Battery",
                        description = "Disables Android Doze & battery throttling for Acoustic Driver so the sound processor isn't paused while gaming or listening with screen off.",
                        isCompleted = isBatteryIgnored,
                        actionLabel = if (isBatteryIgnored) "Configured ✓" else "Request Exemption",
                        onAction = {
                            if (!isBatteryIgnored) {
                                onRequestBatteryOptimization()
                            } else {
                                onToggleBatteryOptimizationDone(false)
                            }
                        },
                        secondaryActionLabel = "App Settings",
                        onSecondaryAction = onOpenAppSettings,
                        manualOverrideLabel = if (!isBatteryIgnored) "Already set to Unrestricted? Tap to mark done" else "Re-check / Clear override",
                        onManualOverride = {
                            if (!isBatteryIgnored) {
                                onToggleBatteryOptimizationDone(true)
                            } else {
                                onToggleBatteryOptimizationDone(false)
                                onRecheckStatus()
                            }
                        }
                    )

                    // Step 3: Lock in Recents
                    SetupStepCard(
                        stepNumber = "3",
                        title = "Lock in Recent Apps",
                        description = "In your phone's Recent Apps screen, tap & hold Acoustic Driver and tap the 'Lock' padlock icon so swiping doesn't kill the audio process.",
                        isCompleted = isLockInRecentsDone,
                        actionLabel = if (isLockInRecentsDone) "Locked in Recents ✓" else "Mark as Locked",
                        onAction = { onToggleLockInRecents(!isLockInRecentsDone) },
                        secondaryActionLabel = "App Info Settings",
                        onSecondaryAction = onOpenAppSettings
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Do It Later", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = BgDark),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (pendingCount == 0) "Finish" else "Done for Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SetupStepCard(
    stepNumber: String,
    title: String,
    description: String,
    isCompleted: Boolean,
    actionLabel: String,
    onAction: () -> Unit,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
    manualOverrideLabel: String? = null,
    onManualOverride: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isCompleted) SuccessGreen.copy(alpha = 0.4f) else BorderSubtle,
                RoundedCornerShape(12.dp)
            )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCompleted) SuccessGreen else CyberCyan),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = BgDark,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Text(
                                text = stepNumber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BgDark
                            )
                        }
                    }

                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                if (isCompleted) {
                    Text(
                        text = "READY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCompleted) SuccessGreen.copy(alpha = 0.2f) else CyberCyan,
                        contentColor = if (isCompleted) SuccessGreen else BgDark
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (isCompleted) "Configured ✓" else actionLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (secondaryActionLabel != null && onSecondaryAction != null) {
                    OutlinedButton(
                        onClick = onSecondaryAction,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricIce),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = secondaryActionLabel, fontSize = 11.sp)
                    }
                }
            }

            if (manualOverrideLabel != null && onManualOverride != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = manualOverrideLabel,
                    fontSize = 11.sp,
                    color = CyberCyan,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { onManualOverride() }
                        .padding(vertical = 2.dp)
                )
            }
        }
    }
}
