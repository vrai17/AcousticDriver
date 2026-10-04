package com.vraidev.acousticdriver.ui.components

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vraidev.acousticdriver.R
import com.vraidev.acousticdriver.audio.model.OutputDevice
import com.vraidev.acousticdriver.ui.theme.*

@Composable
fun TopBar(
    currentOutput: OutputDevice,
    onOutputSelected: (OutputDevice) -> Unit = {},
    onAboutClick: () -> Unit,
    isEnabled: Boolean,
    onToggleEnabled: () -> Unit,
    pendingPersistenceCount: Int = 0,
    onOpenPersistenceSetup: () -> Unit = {}
) {
    val context = LocalContext.current

    Surface(
        color = BgDark,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 16.dp, end = 8.dp, top = 6.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Vrai-Dev branding + Acoustic Driver text matching user red annotation
            Column(
                modifier = Modifier
                    .clickable { onAboutClick() }
                    .padding(vertical = 2.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "ACOUSTIC DRIVER",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    letterSpacing = 0.8.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(1.dp))
                Row(
                    modifier = Modifier.offset(y = 0.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    Text(
                        text = "BY ",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricIce,
                        letterSpacing = 1.sp,
                        modifier = Modifier.offset(y = -6.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.dev_logo),
                        contentDescription = "Vrai-Dev Logo",
                        modifier = Modifier
                            .height(16.dp)
                            .offset(x = -6.dp, y = -7.5.dp) // 👈 Set x here (+ to move right, - to move left)
                    )
                }
            }

            // Action buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Auto-detected Output Device Indicator
                IconButton(
                    onClick = {
                        Toast.makeText(context, "Output: ${currentOutput.displayName}", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(7.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getOutputDeviceIcon(currentOutput),
                            contentDescription = "Output: ${currentOutput.displayName}",
                            tint = ElectricIce,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Background Persistence Setup Alert Button with Badge Number
                IconButton(
                    onClick = onOpenPersistenceSetup,
                    modifier = Modifier.size(36.dp)
                ) {
                    BadgedBox(
                        badge = {
                            if (pendingPersistenceCount > 0) {
                                Badge(
                                    containerColor = AmberWarning,
                                    contentColor = BgDark
                                ) {
                                    Text(
                                        text = "$pendingPersistenceCount",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (pendingPersistenceCount > 0) Icons.Filled.Notifications else Icons.Filled.Shield,
                            contentDescription = "Background Protection Setup",
                            tint = if (pendingPersistenceCount > 0) AmberWarning else CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Master On/Off Toggle Button
                IconButton(
                    onClick = onToggleEnabled,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PowerSettingsNew,
                        contentDescription = "Master Power",
                        tint = if (isEnabled) CyberCyan else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // About Button
                IconButton(
                    onClick = onAboutClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "About Vrai-Dev",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun getOutputDeviceIcon(device: OutputDevice): ImageVector {
    return when (device) {
        OutputDevice.PHONE_SPEAKER -> Icons.Filled.SpeakerPhone
        OutputDevice.WIRED_HEADSET -> Icons.Filled.Headphones
        OutputDevice.USB_DAC -> Icons.Filled.Usb
        OutputDevice.BLUETOOTH -> Icons.Filled.Bluetooth
        OutputDevice.BLUETOOTH_LE -> Icons.Filled.BluetoothAudio
    }
}
