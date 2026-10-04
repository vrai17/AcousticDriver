package com.vraidev.acousticdriver.ui.screens.effects

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vraidev.acousticdriver.audio.AudioEngine
import com.vraidev.acousticdriver.audio.model.VocalFocus
import com.vraidev.acousticdriver.audio.model.VocalTargetMode
import com.vraidev.acousticdriver.ui.components.AcousticSlider
import com.vraidev.acousticdriver.ui.theme.*

@Composable
fun EffectsRackScreen(audioEngine: AudioEngine) {
    val audioState by audioEngine.state.collectAsState()
    val capability by audioEngine.capability.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Signal Chain Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "DSP SIGNAL FLOW",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Input → Preamp → EQ → Bass Boost → Vocal Booster → Compressor → Virtualizer → Limiter → Output",
                    fontSize = 11.sp,
                    color = ElectricIce,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }

        // Module 1: MegaBass / Bass Enhancement
        EffectModuleCard(
            title = "MegaBass™ / Bass Enhancement",
            subtitle = if (capability.hasBassBoost) "Android AudioEffect BassBoost" else "EQ Shelf Fallback",
            enabled = audioState.bassBoostEnabled,
            onToggle = { audioEngine.setBassBoost(it, audioState.bassBoostStrength) }
        ) {
            AcousticSlider(
                label = "Bass Depth & Sub-harmonic Resonance",
                value = audioState.bassBoostStrength.toFloat(),
                onValueChange = { audioEngine.setBassBoost(audioState.bassBoostEnabled, it.toInt()) },
                valueRange = 0f..1000f,
                valueFormatter = { "${(it / 10).toInt()}%" },
                enabled = audioState.bassBoostEnabled,
                accentColor = CyberCyan
            )
        }

        // Module 2: Precision Vocal Clarity & Formant Booster
        EffectModuleCard(
            title = "Vocal Clarity & Formant Booster",
            subtitle = "Linguistic Formant Isolation & Instrument De-Masking",
            enabled = audioState.vocalBoosterEnabled,
            onToggle = { audioEngine.setVocalBooster(it, audioState.vocalBoosterStrength) }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Target Voice Profile Selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "TARGET VOICE PROFILE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        VocalTargetMode.entries.forEach { mode ->
                            FilterChip(
                                selected = audioState.vocalTargetMode == mode,
                                onClick = {
                                    audioEngine.setVocalBooster(
                                        enabled = audioState.vocalBoosterEnabled,
                                        strength = audioState.vocalBoosterStrength,
                                        targetMode = mode,
                                        focus = audioState.vocalIsolationFocus
                                    )
                                },
                                label = {
                                    Text(
                                        text = mode.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (audioState.vocalTargetMode == mode) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonMint.copy(alpha = 0.2f),
                                    selectedLabelColor = NeonMint,
                                    containerColor = SurfaceVariantDark,
                                    labelColor = TextSecondary
                                ),
                                enabled = audioState.vocalBoosterEnabled,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Focus Bandwidth Selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ISOLATION FOCUS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Sweet spot: ${audioState.vocalTargetMode.frequencyLabel}",
                            fontSize = 10.sp,
                            color = NeonMint
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        VocalFocus.entries.forEach { focus ->
                            FilterChip(
                                selected = audioState.vocalIsolationFocus == focus,
                                onClick = {
                                    audioEngine.setVocalBooster(
                                        enabled = audioState.vocalBoosterEnabled,
                                        strength = audioState.vocalBoosterStrength,
                                        targetMode = audioState.vocalTargetMode,
                                        focus = focus
                                    )
                                },
                                label = {
                                    Text(
                                        text = focus.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (audioState.vocalIsolationFocus == focus) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                                    selectedLabelColor = CyberCyan,
                                    containerColor = SurfaceVariantDark,
                                    labelColor = TextSecondary
                                ),
                                enabled = audioState.vocalBoosterEnabled,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                AcousticSlider(
                    label = "Vocal Formant & Presence Lift",
                    value = audioState.vocalBoosterStrength.toFloat(),
                    onValueChange = {
                        audioEngine.setVocalBooster(
                            enabled = audioState.vocalBoosterEnabled,
                            strength = it.toInt(),
                            targetMode = audioState.vocalTargetMode,
                            focus = audioState.vocalIsolationFocus
                        )
                    },
                    valueRange = 0f..100f,
                    valueFormatter = { "${it.toInt()}% (+${"%.1f".format(it * 0.065f)} dB)" },
                    enabled = audioState.vocalBoosterEnabled,
                    accentColor = NeonMint
                )
            }
        }

        // Module 3: Dynamic Range Compression
        EffectModuleCard(
            title = "Dynamic Range Compressor",
            subtitle = if (capability.hasDynamicsProcessing) "Android DynamicsProcessing (MBC)" else "Approximated Peak Leveler",
            enabled = audioState.dynamicsEnabled,
            onToggle = {
                audioEngine.setDynamics(
                    it,
                    audioState.compressorThresholdDb,
                    audioState.compressorRatio,
                    audioState.limiterCeilingDb
                )
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AcousticSlider(
                    label = "Threshold",
                    value = audioState.compressorThresholdDb,
                    onValueChange = {
                        audioEngine.setDynamics(
                            audioState.dynamicsEnabled,
                            it,
                            audioState.compressorRatio,
                            audioState.limiterCeilingDb
                        )
                    },
                    valueRange = -30f..0f,
                    valueFormatter = { "%.1f dB".format(it) },
                    enabled = audioState.dynamicsEnabled,
                    accentColor = ElectricIce
                )

                AcousticSlider(
                    label = "Compression Ratio",
                    value = audioState.compressorRatio,
                    onValueChange = {
                        audioEngine.setDynamics(
                            audioState.dynamicsEnabled,
                            audioState.compressorThresholdDb,
                            it,
                            audioState.limiterCeilingDb
                        )
                    },
                    valueRange = 1f..10f,
                    valueFormatter = { "%.1f : 1".format(it) },
                    enabled = audioState.dynamicsEnabled,
                    accentColor = ElectricIce
                )
            }
        }

        // Module 4: Spatial Virtualizer
        EffectModuleCard(
            title = "3D Spatial Virtualizer",
            subtitle = if (capability.hasVirtualizer) "Binaural Stereo Widener" else "Stereo Matrix Simulation",
            enabled = audioState.virtualizerEnabled,
            onToggle = { audioEngine.setVirtualizer(it, audioState.virtualizerStrength) }
        ) {
            AcousticSlider(
                label = "Stereo Width & Acoustic Expansion",
                value = audioState.virtualizerStrength.toFloat(),
                onValueChange = { audioEngine.setVirtualizer(audioState.virtualizerEnabled, it.toInt()) },
                valueRange = 0f..1000f,
                valueFormatter = { "${(it / 10).toInt()}%" },
                enabled = audioState.virtualizerEnabled,
                accentColor = NeonMint
            )
        }

        // Module 5: Output Limiter / Ceiling
        EffectModuleCard(
            title = "Acoustic Peak Limiter",
            subtitle = "Transducer Overload & Distortion Protection",
            enabled = true,
            onToggle = { /* Always on safeguard */ }
        ) {
            AcousticSlider(
                label = "Ceiling Ceiling Safeguard",
                value = audioState.limiterCeilingDb,
                onValueChange = {
                    audioEngine.setDynamics(
                        audioState.dynamicsEnabled,
                        audioState.compressorThresholdDb,
                        audioState.compressorRatio,
                        it
                    )
                },
                valueRange = -6f..0f,
                valueFormatter = { "%.1f dBFS".format(it) },
                enabled = true,
                accentColor = AmberWarning
            )
        }
    }
}

@Composable
private fun EffectModuleCard(
    title: String,
    subtitle: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (enabled) CyberCyan.copy(alpha = 0.5f) else BorderSubtle,
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (enabled) CyberCyan else TextPrimary
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CyberCyan,
                        checkedTrackColor = CyberCyan.copy(alpha = 0.3f),
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = SurfaceVariantDark
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}
