package com.vraidev.acousticdriver.ui.screens.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vraidev.acousticdriver.R
import com.vraidev.acousticdriver.core.update.UpdateChecker
import com.vraidev.acousticdriver.core.update.UpdateInfo
import com.vraidev.acousticdriver.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AboutScreen(
    onNavigateToDevice: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isCheckingUpdate by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var updateError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Branding Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Developer Logo
                Image(
                    painter = painterResource(id = R.drawable.dev_logo),
                    contentDescription = "Vrai-Dev Logo",
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(75.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "ACOUSTIC DRIVER",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "v${UpdateChecker.CURRENT_VERSION} • Modern Android Audio Platform",
                    fontSize = 12.sp,
                    color = ElectricIce,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Website & Donation Links (Horizontal in-line with authentic scraped brand icons)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Website
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://vraidev.com"))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(CyberCyan)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Language, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Website", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }

                    // Ko-fi (Real brand vector icon)
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ko-fi.com/vraidev"))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5E5B)),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(Color(0xFFFF5E5B))),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_kofi),
                            contentDescription = "Ko-fi Logo",
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFFFF5E5B)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ko-fi", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }

                    // Saweria (Real mascot scraped icon)
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://saweria.co/vraidev"))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE0A938)),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(Color(0xFFE0A938))),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_saweria),
                            contentDescription = "Saweria Logo",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Saweria", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }
        }

        // App Updates Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "APP UPDATES & RELEASE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Installed: v${UpdateChecker.CURRENT_VERSION}",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (!isCheckingUpdate) {
                                isCheckingUpdate = true
                                updateError = null
                                coroutineScope.launch {
                                    val result = UpdateChecker.checkForUpdates()
                                    isCheckingUpdate = false
                                    result.onSuccess { info ->
                                        updateInfo = info
                                    }.onFailure { err ->
                                        updateError = err.message ?: "Failed to check updates"
                                    }
                                }
                            }
                        },
                        enabled = !isCheckingUpdate,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricIce.copy(alpha = 0.2f),
                            contentColor = ElectricIce
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        if (isCheckingUpdate) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                color = ElectricIce,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Checking...", fontSize = 11.sp)
                        } else {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Check Updates", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Update Status Display
                updateInfo?.let { info ->
                    Spacer(modifier = Modifier.height(12.dp))
                    if (info.isUpdateAvailable) {
                        Surface(
                            color = CyberCyan.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.SystemUpdate, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Update Available: ${info.latestVersion}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberCyan
                                    )
                                }
                                if (info.releaseNotes.isNotBlank()) {
                                    Text(
                                        text = info.releaseNotes,
                                        fontSize = 11.sp,
                                        color = TextPrimary,
                                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                                        maxLines = 3
                                    )
                                }
                                Button(
                                    onClick = { UpdateChecker.openUpdateUrl(context, info.downloadUrl) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = BgDark),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Download & Install Update (.apk)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFF00F5D4), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Acoustic Driver is up to date!", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }

                updateError?.let { err ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Error: $err", fontSize = 11.sp, color = androidx.compose.ui.graphics.Color(0xFFFF5E5B))
                }
            }
        }

        // Hardware Diagnostics Link Card
        if (onNavigateToDevice != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToDevice() }
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = BgDark,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Smartphone, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                            }
                        }
                        Column {
                            Text("DEVICE AUDIOPHILE DIAGNOSTICS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyberCyan, letterSpacing = 0.5.sp)
                            Text("Inspect Audio HAL, DSP pipeline & UUIDs", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                }
            }
        }

        // Philosophy & Heritage Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ACOUSTIC MODDING HERITAGE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Inspired by the classic Sony Ericsson phone modding culture (W810i, K750i, W995), this platform bridges legacy acoustic tuning with modern Android DSP architecture.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Rather than flattening everything into a simple graphic EQ, our Acoustic Intermediate Representation (AIR) normalizes multiband dynamics, limiters, bass enhancement, and speaker gains with transparent compatibility reporting.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = TextSecondary
                )
            }
        }

        // Developer Info Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "DEVELOPER SPECIFICATIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                AboutInfoRow("Developer", "Vrai-Dev")
                AboutInfoRow("Website", "vraidev.com")
                AboutInfoRow("Core Engine", "Vrai-Dev AIR Audio DSP v1.0")
                AboutInfoRow("Target Platform", "Android 9.0+ (API 28 - API 35)")
                AboutInfoRow("License", "MIT with Rebrand Prohibition")
            }
        }
    }
}

@Composable
private fun AboutInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = TextSecondary)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}
