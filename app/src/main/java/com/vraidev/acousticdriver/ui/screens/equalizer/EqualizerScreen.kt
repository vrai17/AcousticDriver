package com.vraidev.acousticdriver.ui.screens.equalizer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vraidev.acousticdriver.audio.AudioEngine
import com.vraidev.acousticdriver.core.air.*
import com.vraidev.acousticdriver.data.repository.DriverRepository
import com.vraidev.acousticdriver.ui.components.AcousticSlider
import com.vraidev.acousticdriver.ui.components.CyberWaveform
import com.vraidev.acousticdriver.ui.theme.*

@Composable
fun EqualizerScreen(
    audioEngine: AudioEngine,
    repository: DriverRepository? = null
) {
    val audioState by audioEngine.state.collectAsState()
    val capability by audioEngine.capability.collectAsState()

    var selectedMode by remember { mutableStateOf(0) } // 0 = Graphic 10-Band, 1 = Parametric
    val standardFrequencies = listOf(31, 62, 125, 250, 500, 1000, 2000, 4000, 8000, 16000)

    var showSaveDialog by remember { mutableStateOf(false) }
    var profileName by remember { mutableStateOf("") }
    var profileNotes by remember { mutableStateOf("") }

    val currentGains = remember(audioState.eqBandGains) {
        val list = audioState.eqBandGains.toMutableList()
        while (list.size < standardFrequencies.size) list.add(0f)
        list.take(standardFrequencies.size)
    }

    if (showSaveDialog && repository != null) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(
                    text = "Save Custom Profile",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Save your current 10-band equalizer curve and active effects as a named acoustic profile.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = profileName,
                        onValueChange = { profileName = it },
                        label = { Text("Profile Name", fontSize = 12.sp) },
                        placeholder = { Text("e.g. My Custom Bass Curve", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            focusedContainerColor = SurfaceVariantDark,
                            unfocusedContainerColor = SurfaceVariantDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = profileNotes,
                        onValueChange = { profileNotes = it },
                        label = { Text("Description (Optional)", fontSize = 12.sp) },
                        placeholder = { Text("Tuning description...", fontSize = 12.sp) },
                        singleLine = false,
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            focusedContainerColor = SurfaceVariantDark,
                            unfocusedContainerColor = SurfaceVariantDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val base = audioState.activeDriver ?: AcousticIntermediateRepresentation(
                            metadata = DriverMetadata(
                                id = "custom-temp",
                                name = profileName.ifBlank { "Custom Tuning" },
                                author = "User",
                                sourceDevice = "Custom Device",
                                version = "1.0",
                                description = profileNotes
                            )
                        )
                        val eqBands = standardFrequencies.mapIndexed { idx, freq ->
                            EqBand(freq, currentGains.getOrElse(idx) { 0f })
                        }
                        val air = base.copy(
                            preampDb = audioState.preampDb,
                            masterGainDb = audioState.volumeCompensationDb,
                            graphicEq = eqBands,
                            bassBoost = if (audioState.bassBoostEnabled) BassConfig(true, audioState.bassBoostStrength) else null,
                            compressor = if (audioState.dynamicsEnabled) CompressorConfig(true, audioState.compressorThresholdDb, audioState.compressorRatio) else null,
                            limiter = LimiterConfig(true, audioState.limiterCeilingDb),
                            stereoSpatial = if (audioState.virtualizerEnabled) SpatialConfig(true, audioState.virtualizerStrength) else null
                        )
                        val saved = repository.saveCustomProfile(profileName, profileNotes, air)
                        audioEngine.applyDriver(saved)
                        showSaveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = BgDark)
                ) {
                    Text("Save Profile", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header & Mode Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PRECISION EQUALIZER",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (selectedMode == 0) "10-Band Graphic Tuning" else "Parametric Multi-Filter",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (repository != null) {
                    IconButton(
                        onClick = { showSaveDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.BookmarkAdd,
                            contentDescription = "Save Custom Profile",
                            tint = CyberCyan
                        )
                    }
                }

                IconButton(
                    onClick = {
                        currentGains.indices.forEach { idx ->
                            audioEngine.setEqBandGain(idx, 0f)
                        }
                    }
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Reset Flat", tint = TextSecondary)
                }
            }
        }

        // Mode Tab Bar
        TabRow(
            selectedTabIndex = selectedMode,
            containerColor = SurfaceDark,
            contentColor = CyberCyan,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedMode == 0,
                onClick = { selectedMode = 0 },
                text = { Text("Graphic EQ", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = selectedMode == 1,
                onClick = { selectedMode = 1 },
                text = { Text("Parametric EQ", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
            )
        }

        // Response Curve Preview
        CyberWaveform(
            gains = currentGains,
            isActive = audioState.isEnabled && !audioState.isBypassActive
        )

        if (selectedMode == 0) {
            // Graphic EQ View (10 Bands)
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "FREQUENCY BANDS (±12 dB)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )

                    standardFrequencies.forEachIndexed { index, freq ->
                        val gain = currentGains.getOrElse(index) { 0f }
                        val freqLabel = if (freq >= 1000) "${freq / 1000} kHz" else "$freq Hz"

                        AcousticSlider(
                            label = freqLabel,
                            value = gain,
                            onValueChange = { newGain ->
                                audioEngine.setEqBandGain(index, newGain)
                            },
                            valueRange = -12f..12f,
                            valueFormatter = { "%+.1f dB".format(it) },
                            accentColor = if (gain > 0) CyberCyan else if (gain < 0) ElectricIce else TextSecondary
                        )
                    }
                }
            }
        } else {
            // Parametric EQ View
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "PARAMETRIC FILTERS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        letterSpacing = 1.sp
                    )

                    // Filter 1: Low Shelf
                    FilterControlCard(
                        name = "Filter 1 — Low Shelf",
                        type = "Low-Shelf (Sub-Bass)",
                        defaultFreq = 80f,
                        defaultGain = 3.5f,
                        defaultQ = 0.71f
                    )

                    // Filter 2: Peaking Mid
                    FilterControlCard(
                        name = "Filter 2 — Mid Range Peak",
                        type = "Peaking / Bell",
                        defaultFreq = 1200f,
                        defaultGain = -1.5f,
                        defaultQ = 1.2f
                    )

                    // Filter 3: High Shelf
                    FilterControlCard(
                        name = "Filter 3 — High Shelf Presence",
                        type = "High-Shelf (Air / Treble)",
                        defaultFreq = 8500f,
                        defaultGain = 2.0f,
                        defaultQ = 0.71f
                    )
                }
            }
        }

        // Quick Preset Chips
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "EQ CURVE PRESETS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                val presets = listOf(
                    "Flat" to listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
                    "Mega Bass" to listOf(6f, 5f, 3.5f, 1f, 0f, 0f, 1.5f, 3f, 4.5f, 5f),
                    "Vocal" to listOf(-2f, -1f, 0f, 1f, 3f, 4f, 2.5f, 1f, 0f, 0f),
                    "Rock" to listOf(4.5f, 3f, 1.5f, 0f, -1f, 0.5f, 2f, 3.5f, 4f, 4.5f),
                    "Club Electronic" to listOf(5.5f, 4.5f, 2f, 0f, -0.5f, 1f, 2.5f, 4f, 5f, 6f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.take(3).forEach { (name, curve) ->
                        OutlinedButton(
                            onClick = {
                                curve.forEachIndexed { i, g -> audioEngine.setEqBandGain(i, g) }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricIce),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(name, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterControlCard(
    name: String,
    type: String,
    defaultFreq: Float,
    defaultGain: Float,
    defaultQ: Float
) {
    var freq by remember { mutableStateOf(defaultFreq) }
    var gain by remember { mutableStateOf(defaultGain) }
    var q by remember { mutableStateOf(defaultQ) }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(type, fontSize = 11.sp, color = ElectricIce)
            }

            Spacer(modifier = Modifier.height(8.dp))

            AcousticSlider(
                label = "Frequency",
                value = freq,
                onValueChange = { freq = it },
                valueRange = 20f..20000f,
                valueFormatter = { if (it >= 1000) "%.1f kHz".format(it / 1000) else "%.0f Hz".format(it) },
                accentColor = CyberCyan
            )

            AcousticSlider(
                label = "Gain",
                value = gain,
                onValueChange = { gain = it },
                valueRange = -12f..12f,
                valueFormatter = { "%+.1f dB".format(it) },
                accentColor = ElectricIce
            )

            AcousticSlider(
                label = "Q Factor (Bandwidth)",
                value = q,
                onValueChange = { q = it },
                valueRange = 0.1f..5f,
                valueFormatter = { "Q = %.2f".format(it) },
                accentColor = AmberWarning
            )
        }
    }
}
