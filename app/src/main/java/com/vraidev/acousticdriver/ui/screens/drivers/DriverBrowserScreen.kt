package com.vraidev.acousticdriver.ui.screens.drivers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.vraidev.acousticdriver.core.air.*
import com.vraidev.acousticdriver.core.translator.AcousticTranslator
import com.vraidev.acousticdriver.data.repository.DriverRepository
import com.vraidev.acousticdriver.ui.components.CompatibilityBadge
import com.vraidev.acousticdriver.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverBrowserScreen(
    audioEngine: AudioEngine,
    repository: DriverRepository,
    translator: AcousticTranslator
) {
    val drivers by repository.drivers.collectAsState()
    val favoriteIds by repository.favoriteIds.collectAsState()
    val audioState by audioEngine.state.collectAsState()
    val capability by audioEngine.capability.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var inspectingDriver by remember { mutableStateOf<AcousticIntermediateRepresentation?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var driverToDelete by remember { mutableStateOf<AcousticIntermediateRepresentation?>(null) }

    val categories = listOf("All", "Custom", "Sony Ericsson", "Bass", "Walkman", "Cyber-shot", "Hi-Fi", "Loudness", "Favorites")

    val filteredDrivers = remember(drivers, searchQuery, selectedCategory, favoriteIds) {
        drivers.filter { d ->
            val matchesSearch = d.metadata.name.contains(searchQuery, ignoreCase = true) ||
                    d.metadata.sourceDevice.contains(searchQuery, ignoreCase = true) ||
                    d.metadata.tags.any { it.contains(searchQuery, ignoreCase = true) }

            val matchesCategory = when (selectedCategory) {
                "All" -> true
                "Favorites" -> favoriteIds.contains(d.metadata.id)
                "Custom" -> d.metadata.tags.any { it.equals("Custom", ignoreCase = true) } || d.metadata.id.startsWith("custom-")
                else -> d.metadata.tags.any { it.equals(selectedCategory, ignoreCase = true) }
            }
            matchesSearch && matchesCategory
        }
    }

    if (inspectingDriver != null) {
        val score = translator.analyzeCompatibility(inspectingDriver!!, capability)
        TranslationReportDialog(
            driver = inspectingDriver!!,
            score = score,
            onDismiss = { inspectingDriver = null }
        )
    }

    if (driverToDelete != null) {
        AlertDialog(
            onDismissRequest = { driverToDelete = null },
            title = {
                Text(
                    text = "Delete Custom Profile?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove \"${driverToDelete?.metadata?.name}\"? This action cannot be undone.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        driverToDelete?.let { d ->
                            repository.deleteDriver(d.metadata.id)
                        }
                        driverToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed, contentColor = TextPrimary)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { driverToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }

    if (showAddDialog) {
        AddOrImportDriverDialog(
            audioEngine = audioEngine,
            onSaveCustom = { name, notes ->
                val base = audioState.activeDriver ?: AcousticIntermediateRepresentation(
                    metadata = DriverMetadata(
                        id = "custom-temp",
                        name = name,
                        author = "User",
                        sourceDevice = "Custom Device",
                        version = "1.0",
                        description = notes
                    )
                )
                val standardFrequencies = audioEngine.standardFrequencies
                val currentEq = standardFrequencies.mapIndexed { idx, freq ->
                    EqBand(freq, audioState.eqBandGains.getOrElse(idx) { 0f })
                }
                val air = base.copy(
                    preampDb = audioState.preampDb,
                    masterGainDb = audioState.volumeCompensationDb,
                    graphicEq = currentEq,
                    bassBoost = if (audioState.bassBoostEnabled) BassConfig(true, audioState.bassBoostStrength) else null,
                    compressor = if (audioState.dynamicsEnabled) CompressorConfig(true, audioState.compressorThresholdDb, audioState.compressorRatio) else null,
                    limiter = LimiterConfig(true, audioState.limiterCeilingDb),
                    stereoSpatial = if (audioState.virtualizerEnabled) SpatialConfig(true, audioState.virtualizerStrength) else null
                )
                val saved = repository.saveCustomProfile(name, notes, air)
                audioEngine.applyDriver(saved)
                selectedCategory = "Custom"
                showAddDialog = false
            },
            onImport = { content ->
                val res = repository.importDriver(content)
                if (res.isSuccess) {
                    res.getOrNull()?.let { audioEngine.applyDriver(it) }
                }
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    Scaffold(
        containerColor = BgDark,
        contentWindowInsets = WindowInsets(0.dp), // Eliminates duplicate system inset empty gap above bottom navbar
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = CyberCyan,
                contentColor = BgDark
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add or Save Profile")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search acoustic drivers by device, author, tag…", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = null, tint = TextSecondary)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = BorderSubtle,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                            selectedLabelColor = CyberCyan,
                            containerColor = SurfaceDark,
                            labelColor = TextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${filteredDrivers.size} ACOUSTIC PROFILES AVAILABLE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Driver List - fills screen normally with bottom padding for FAB
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 88.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredDrivers, key = { it.metadata.id }) { driver ->
                    val isCurrent = audioState.activeDriver?.metadata?.id == driver.metadata.id
                    val isFav = favoriteIds.contains(driver.metadata.id)
                    val isCustom = driver.metadata.id.startsWith("custom-") || driver.metadata.tags.any { it.equals("Custom", ignoreCase = true) }
                    val score = translator.analyzeCompatibility(driver, capability)

                    DriverCard(
                        driver = driver,
                        isActive = isCurrent,
                        isFavorite = isFav,
                        isCustom = isCustom,
                        scorePercentage = score.percentage,
                        onApply = { audioEngine.applyDriver(driver) },
                        onInspect = { inspectingDriver = driver },
                        onToggleFavorite = { repository.toggleFavorite(driver.metadata.id) },
                        onDelete = { driverToDelete = driver }
                    )
                }
            }
        }
    }
}

@Composable
private fun DriverCard(
    driver: AcousticIntermediateRepresentation,
    isActive: Boolean,
    isFavorite: Boolean,
    isCustom: Boolean,
    scorePercentage: Int,
    onApply: () -> Unit,
    onInspect: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isActive) CyberCyan else BorderSubtle,
                RoundedCornerShape(14.dp)
            )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = driver.metadata.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) CyberCyan else TextPrimary,
                        maxLines = 1
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isCustom) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = "Delete Profile",
                                tint = AccentRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    CompatibilityBadge(
                        percentage = scorePercentage,
                        isActive = isActive,
                        onClick = onInspect
                    )

                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = null,
                            tint = if (isFavorite) AmberWarning else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = "${driver.metadata.sourceDevice} • By ${driver.metadata.author} (${driver.metadata.year})",
                fontSize = 12.sp,
                color = ElectricIce,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            Text(
                text = driver.metadata.description,
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 2,
                modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
            )

            // Tags row
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                driver.metadata.tags.take(3).forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (tag.equals("Custom", ignoreCase = true)) CyberCyan.copy(alpha = 0.2f) else SurfaceVariantDark
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tag,
                            fontSize = 10.sp,
                            color = if (tag.equals("Custom", ignoreCase = true)) CyberCyan else TextSecondary,
                            fontWeight = if (tag.equals("Custom", ignoreCase = true)) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onApply,
                    enabled = !isActive,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = BgDark,
                        disabledContainerColor = SurfaceVariantDark,
                        disabledContentColor = TextMuted
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isActive) "Applied" else "Apply Profile", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onInspect,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricIce),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Translation Info", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun AddOrImportDriverDialog(
    audioEngine: AudioEngine,
    onSaveCustom: (name: String, notes: String) -> Unit,
    onImport: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Save Current, 1 = Import Text
    var profileName by remember { mutableStateOf("") }
    var profileNotes by remember { mutableStateOf("") }

    var rawText by remember {
        mutableStateOf(
            """
            [Acoustic_Driver]
            Name=Mega Bass Turbo Custom
            Device=Sony Ericsson W810i
            Author=Custom Tuner
            Speaker_Gain=0x08
            Spk_EQ_Band1=60, 6.0
            Spk_EQ_Band2=250, 3.0
            Spk_EQ_Band3=1000, 0.0
            Spk_EQ_Band4=4000, 2.5
            Spk_EQ_Band5=12000, 4.5
            Bass_Boost=1
            Bass_Level=800
            Compressor_Threshold=-14
            Compressor_Ratio=3.0
            """.trimIndent()
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Acoustic Profiles",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SurfaceVariantDark,
                    contentColor = CyberCyan,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Save Current", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Import Legacy", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }
        },
        text = {
            if (selectedTab == 0) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Saves your active 10-band equalizer, bass boost, dynamic limiter, and preamp calibration as a new custom driver.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = profileName,
                        onValueChange = { profileName = it },
                        label = { Text("Profile Name", fontSize = 12.sp) },
                        placeholder = { Text("e.g. My Bass Tuning", fontSize = 12.sp) },
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
                        label = { Text("Notes / Tuning Description", fontSize = 12.sp) },
                        placeholder = { Text("Optional description...", fontSize = 12.sp) },
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
            } else {
                Column {
                    Text(
                        text = "Paste legacy Sony Ericsson acoustic parameters (.ini / text) or AIR JSON:",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = rawText,
                        onValueChange = { rawText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 11.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceVariantDark,
                            unfocusedContainerColor = SurfaceVariantDark
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedTab == 0) {
                        onSaveCustom(profileName.ifBlank { "Custom Tuning" }, profileNotes)
                    } else {
                        onImport(rawText)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = BgDark)
            ) {
                Text(if (selectedTab == 0) "Save Profile" else "Parse & Import", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = SurfaceDark
    )
}
