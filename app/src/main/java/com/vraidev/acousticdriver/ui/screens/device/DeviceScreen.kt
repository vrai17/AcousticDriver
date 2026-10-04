package com.vraidev.acousticdriver.ui.screens.device

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vraidev.acousticdriver.audio.AudioEngine
import com.vraidev.acousticdriver.audio.model.DeviceCapability
import com.vraidev.acousticdriver.ui.theme.*

@Composable
fun DeviceScreen(audioEngine: AudioEngine) {
    val capability by audioEngine.capability.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Device Header Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "DEVICE AUDIOPHILE ARCHITECTURE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${capability.manufacturer} ${capability.model}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = "SoC: ${capability.soc} • Android ${capability.androidVersion} (API ${capability.androidVersion})",
                    fontSize = 12.sp,
                    color = ElectricIce
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoPill(label = "HAL Architecture", value = capability.audioHalType)
                    InfoPill(label = "DSP Pipeline", value = "Standard AOSP")
                }
            }
        }

        // Audio Effects Availability Matrix
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "AOSP AUDIO EFFECT PIPELINE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                CapabilityRow(
                    name = "Equalizer Engine",
                    supported = capability.hasEqualizer,
                    detail = "${capability.equalizerBands} hardware bands (${capability.bandFrequenciesHz.joinToString(", ") { if (it >= 1000) "${it/1000}k" else "$it" }} Hz)"
                )

                CapabilityRow(
                    name = "Dynamics Processing (MBC & Limiter)",
                    supported = capability.hasDynamicsProcessing,
                    detail = "API 28+ native multichannel dynamic compression"
                )

                CapabilityRow(
                    name = "Bass Boost Engine",
                    supported = capability.hasBassBoost,
                    detail = "AOSP AudioEffect BassBoost"
                )

                CapabilityRow(
                    name = "Spatial Audio / Virtualizer",
                    supported = capability.hasVirtualizer,
                    detail = "Binaural headphone & speaker widening"
                )

                CapabilityRow(
                    name = "Loudness Enhancer",
                    supported = capability.hasLoudnessEnhancer,
                    detail = "Volume compensator with digital headroom"
                )
            }
        }

        // Discovered System Effect UUIDs
        if (capability.discoveredEffects.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DISCOVERED SYSTEM AUDIO EFFECTS (${capability.discoveredEffects.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    capability.discoveredEffects.take(5).forEach { effect ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(effect.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text("Impl: ${effect.implementor}", fontSize = 10.sp, color = TextSecondary)
                            }
                            Text(
                                effect.uuid.take(8) + "…",
                                fontSize = 10.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = ElectricIce
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoPill(label: String, value: String, isHighlight: Boolean = false) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceVariantDark)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Column {
            Text(label, fontSize = 9.sp, color = TextMuted)
            Text(
                value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isHighlight) CyberCyan else TextPrimary
            )
        }
    }
}

@Composable
private fun CapabilityRow(name: String, supported: Boolean, detail: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (supported) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
            contentDescription = null,
            tint = if (supported) SuccessGreen else AccentRed,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = detail,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}
