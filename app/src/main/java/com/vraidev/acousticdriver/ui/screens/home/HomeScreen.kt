package com.vraidev.acousticdriver.ui.screens.home

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vraidev.acousticdriver.audio.AudioEngine
import com.vraidev.acousticdriver.core.air.AcousticIntermediateRepresentation
import com.vraidev.acousticdriver.core.translator.AcousticTranslator
import com.vraidev.acousticdriver.ui.components.AcousticSlider
import com.vraidev.acousticdriver.ui.components.CompatibilityBadge
import com.vraidev.acousticdriver.ui.components.CyberWaveform
import com.vraidev.acousticdriver.ui.screens.drivers.TranslationReportDialog
import com.vraidev.acousticdriver.ui.theme.*

@Composable
fun HomeScreen(
    audioEngine: AudioEngine,
    translator: AcousticTranslator,
    onNavigateToDrivers: () -> Unit,
    onNavigateToEq: () -> Unit,
    onNavigateToEffects: () -> Unit
) {
    val audioState by audioEngine.state.collectAsState()
    val capability by audioEngine.capability.collectAsState()
    val activeDriver = audioState.activeDriver

    var showReportDialog by remember { mutableStateOf(false) }

    val compatScore = remember(activeDriver, capability) {
        if (activeDriver != null) {
            translator.analyzeCompatibility(activeDriver, capability)
        } else null
    }

    if (showReportDialog && activeDriver != null && compatScore != null) {
        TranslationReportDialog(
            driver = activeDriver,
            score = compatScore,
            onDismiss = { showReportDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Profile Card
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
                    Text(
                        text = "ACTIVE ACOUSTIC PROFILE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        letterSpacing = 1.sp
                    )

                    if (compatScore != null) {
                        CompatibilityBadge(
                            percentage = compatScore.percentage,
                            isActive = true,
                            onClick = { showReportDialog = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = activeDriver?.metadata?.name ?: "No Profile Active",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = activeDriver?.metadata?.sourceDevice ?: "Stock Android Output",
                    fontSize = 13.sp,
                    color = ElectricIce
                )

                if (activeDriver != null) {
                    Text(
                        text = activeDriver.metadata.description,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onNavigateToDrivers,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = BgDark),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.LibraryMusic, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Browse Drivers", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = { showReportDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricIce),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Assessment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Translation", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Live Audio Spectrum / Frequency Response Waveform
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACOUSTIC RESPONSE CURVE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (audioState.isBypassActive) "BYPASS (STOCK)" else "DSP ACTIVE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (audioState.isBypassActive) AmberWarning else CyberCyan
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            CyberWaveform(
                gains = audioState.eqBandGains,
                isActive = audioState.isEnabled && !audioState.isBypassActive
            )
        }

        // A/B Comparison Card
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
                            text = "A/B COMPARISON",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                        Text(
                            text = "Instant comparison against unmodified stock audio",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Switch(
                        checked = !audioState.isBypassActive,
                        onCheckedChange = { audioEngine.setBypass(!it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberCyan,
                            checkedTrackColor = CyberCyan.copy(alpha = 0.3f),
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = SurfaceVariantDark
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !audioState.isBypassActive,
                        onClick = { audioEngine.setBypass(false) },
                        label = { Text("A: Acoustic Driver", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                            selectedLabelColor = CyberCyan
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = audioState.isBypassActive,
                        onClick = { audioEngine.setBypass(true) },
                        label = { Text("B: Stock Bypass", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberWarning.copy(alpha = 0.2f),
                            selectedLabelColor = AmberWarning
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Controls
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "QUICK SOUND CONTROLS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    letterSpacing = 1.sp
                )

                // Bass Boost Slider
                AcousticSlider(
                    label = "Bass Enhancement",
                    value = audioState.bassBoostStrength.toFloat(),
                    onValueChange = { audioEngine.setBassBoost(it > 0, it.toInt()) },
                    valueRange = 0f..1000f,
                    valueFormatter = { "${(it / 10).toInt()}%" },
                    accentColor = CyberCyan
                )

                // Preamp Volume Slider
                AcousticSlider(
                    label = "Software Headroom / Preamp",
                    value = audioState.preampDb,
                    onValueChange = { /* Preamp adjust */ },
                    valueRange = -6f..6f,
                    valueFormatter = { "%+.1f dB".format(it) },
                    accentColor = ElectricIce
                )

                // Navigation buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateToEq,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Equalizer, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("10-Band EQ", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onNavigateToEffects,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Effects Rack", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
