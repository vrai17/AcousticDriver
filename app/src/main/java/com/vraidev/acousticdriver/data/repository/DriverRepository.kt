package com.vraidev.acousticdriver.data.repository

import android.content.Context
import com.vraidev.acousticdriver.core.air.AcousticIntermediateRepresentation
import com.vraidev.acousticdriver.core.air.DriverMetadata
import com.vraidev.acousticdriver.core.parser.AcousticDriverParser
import com.vraidev.acousticdriver.core.parser.JsonAirParser
import com.vraidev.acousticdriver.core.parser.LegacySonyEricssonParser
import com.vraidev.acousticdriver.data.bundled.BundledDrivers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DriverRepository(private val context: Context? = null) {

    private val legacyParser: AcousticDriverParser = LegacySonyEricssonParser()
    private val jsonParser: JsonAirParser = JsonAirParser()
    private val prefs = context?.getSharedPreferences("acoustic_custom_drivers", Context.MODE_PRIVATE)

    private val _drivers = MutableStateFlow<List<AcousticIntermediateRepresentation>>(loadInitialDrivers())
    val drivers: StateFlow<List<AcousticIntermediateRepresentation>> = _drivers.asStateFlow()

    private val _favoriteIds = MutableStateFlow<Set<String>>(loadFavorites())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    private fun loadInitialDrivers(): List<AcousticIntermediateRepresentation> {
        val customDrivers = mutableListOf<AcousticIntermediateRepresentation>()
        prefs?.getStringSet("custom_drivers_json", null)?.forEach { jsonStr ->
            try {
                if (jsonParser.canParse(jsonStr)) {
                    customDrivers.add(jsonParser.parse(jsonStr))
                }
            } catch (_: Exception) {}
        }
        return customDrivers + BundledDrivers.catalog
    }

    private fun loadFavorites(): Set<String> {
        return prefs?.getStringSet("favorites", null) ?: setOf("se-w810-mega-acoustic")
    }

    private fun saveCustomDriversToStorage() {
        val customDrivers = _drivers.value.filter {
            it.metadata.id.startsWith("custom-") || it.metadata.tags.contains("Custom")
        }
        val jsonSet = customDrivers.mapNotNull { air ->
            try {
                jsonParser.serialize(air)
            } catch (_: Exception) {
                null
            }
        }.toSet()
        prefs?.edit()?.putStringSet("custom_drivers_json", jsonSet)?.apply()
    }

    fun getDriverById(id: String): AcousticIntermediateRepresentation? {
        return _drivers.value.find { it.metadata.id == id }
    }

    fun toggleFavorite(driverId: String) {
        _favoriteIds.update { current ->
            val updated = if (current.contains(driverId)) current - driverId else current + driverId
            prefs?.edit()?.putStringSet("favorites", updated)?.apply()
            updated
        }
    }

    fun saveCustomProfile(
        name: String,
        notes: String = "",
        baseAir: AcousticIntermediateRepresentation
    ): AcousticIntermediateRepresentation {
        val id = "custom-${System.currentTimeMillis()}"
        val customAir = baseAir.copy(
            metadata = DriverMetadata(
                id = id,
                name = name.ifBlank { "Custom Tuning" },
                author = "User",
                sourceDevice = "Custom Profile",
                version = "1.0",
                description = notes.ifBlank { "User custom tuned acoustic profile." },
                tags = listOf("Custom", "User"),
                year = "2026"
            )
        )
        _drivers.update { current ->
            listOf(customAir) + current.filter { it.metadata.id != id }
        }
        saveCustomDriversToStorage()
        return customAir
    }

    fun deleteDriver(driverId: String) {
        _drivers.update { current ->
            current.filter { it.metadata.id != driverId }
        }
        _favoriteIds.update { it - driverId }
        saveCustomDriversToStorage()
    }

    fun importDriver(content: String): Result<AcousticIntermediateRepresentation> {
        return try {
            val air = if (jsonParser.canParse(content)) {
                jsonParser.parse(content)
            } else if (legacyParser.canParse(content)) {
                legacyParser.parse(content)
            } else {
                legacyParser.parse(content) // fallback attempt
            }
            _drivers.update { current ->
                val filtered = current.filter { it.metadata.id != air.metadata.id }
                listOf(air) + filtered
            }
            if (air.metadata.id.startsWith("custom-") || air.metadata.tags.contains("Custom")) {
                saveCustomDriversToStorage()
            }
            Result.success(air)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
