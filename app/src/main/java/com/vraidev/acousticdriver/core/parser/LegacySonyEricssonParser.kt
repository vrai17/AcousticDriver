package com.vraidev.acousticdriver.core.parser

import com.vraidev.acousticdriver.core.air.*
import java.util.UUID

class LegacySonyEricssonParser : AcousticDriverParser {

    override fun canParse(content: String): Boolean {
        val lower = content.lowercase()
        return lower.contains("[acoustic") ||
                lower.contains("spk_eq") ||
                lower.contains("speaker_gain") ||
                lower.contains("ear_gain") ||
                lower.contains("megabass") ||
                lower.contains("bass_boost") ||
                lower.contains("acoustic_driver")
    }

    override fun parse(content: String): AcousticIntermediateRepresentation {
        val lines = content.lines()
        val rawParams = mutableMapOf<String, String>()
        var currentSection = "DEFAULT"

        var driverName = "Legacy Acoustic Driver"
        var author = "Sony Ericsson Modder"
        var sourceDevice = "Sony Ericsson Classic"
        var description = "Imported legacy acoustic driver"
        var preampDb = 0f
        var masterGainDb = 0f

        val eqBands = mutableListOf<EqBand>()
        var bassEnabled = false
        var bassStrength = 500
        var bassCutoff = 100

        var compEnabled = false
        var compThreshold = -12f
        var compRatio = 2.0f
        var compAttack = 10f
        var compRelease = 100f

        var limiterEnabled = false
        var limiterCeiling = -0.5f

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith(";") || line.startsWith("#")) continue

            if (line.startsWith("[") && line.endsWith("]")) {
                currentSection = line.substring(1, line.length - 1).trim()
                continue
            }

            val eqIndex = line.indexOf('=')
            if (eqIndex > 0) {
                val key = line.substring(0, eqIndex).trim().lowercase()
                val value = line.substring(eqIndex + 1).trim()
                rawParams["$currentSection.$key"] = value

                when (key) {
                    "name", "driver_name", "title" -> driverName = value
                    "author", "by", "creator" -> author = value
                    "phone", "device", "model", "source_device" -> sourceDevice = value
                    "desc", "description" -> description = value
                    "preamp", "pre_amp", "preamp_db" -> preampDb = parseFloatSafe(value, 0f)
                    "master_gain", "output_gain", "gain_db" -> masterGainDb = parseFloatSafe(value, 0f)
                    "speaker_gain", "ear_gain" -> {
                        val gainVal = parseGainHexOrDec(value)
                        masterGainDb = gainVal.coerceIn(-6f, 6f)
                    }
                    "bass_boost", "megabass", "bass_enhancement" -> {
                        bassEnabled = parseBoolean(value)
                    }
                    "bass_level", "bass_strength" -> {
                        bassStrength = parseIntSafe(value, 500).coerceIn(0, 1000)
                        bassEnabled = true
                    }
                    "bass_cutoff", "bass_freq" -> {
                        bassCutoff = parseIntSafe(value, 100).coerceIn(40, 250)
                    }
                    "compressor_enabled", "drc_enabled" -> compEnabled = parseBoolean(value)
                    "compressor_threshold", "comp_threshold", "drc_threshold" -> {
                        compThreshold = parseFloatSafe(value, -12f)
                        compEnabled = true
                    }
                    "compressor_ratio", "comp_ratio" -> {
                        compRatio = parseFloatSafe(value, 2.0f).coerceIn(1f, 10f)
                        compEnabled = true
                    }
                    "compressor_attack", "comp_attack" -> compAttack = parseFloatSafe(value, 10f)
                    "compressor_release", "comp_release" -> compRelease = parseFloatSafe(value, 100f)
                    "limiter_enabled" -> limiterEnabled = parseBoolean(value)
                    "limiter_ceiling", "limiter_threshold" -> {
                        limiterCeiling = parseFloatSafe(value, -0.5f)
                        limiterEnabled = true
                    }
                    else -> {
                        if (key.startsWith("spk_eq") || key.startsWith("eq_band") || key.startsWith("band")) {
                            parseBand(value)?.let { eqBands.add(it) }
                        }
                    }
                }
            }
        }

        // If no explicit EQ bands were parsed, provide standard 5-band default curve if gain was specified
        val finalEqBands = if (eqBands.isNotEmpty()) {
            eqBands.sortedBy { it.frequencyHz }
        } else {
            listOf(
                EqBand(60, 3.0f),
                EqBand(250, 1.5f),
                EqBand(1000, 0.0f),
                EqBand(4000, 1.5f),
                EqBand(12000, 3.0f)
            )
        }

        val metadata = DriverMetadata(
            id = "imported-" + UUID.nameUUIDFromBytes(content.toByteArray()).toString().take(8),
            name = driverName,
            author = author,
            sourceDevice = sourceDevice,
            version = "1.0",
            description = description,
            tags = listOf("Legacy", sourceDevice.lowercase().replace(" ", "-"), "Community")
        )

        return AcousticIntermediateRepresentation(
            metadata = metadata,
            preampDb = preampDb,
            masterGainDb = masterGainDb,
            graphicEq = finalEqBands,
            bassBoost = if (bassEnabled) BassConfig(true, bassStrength, bassCutoff) else null,
            compressor = if (compEnabled) CompressorConfig(true, compThreshold, compRatio, compAttack, compRelease) else null,
            limiter = if (limiterEnabled) LimiterConfig(true, limiterCeiling) else null,
            rawLegacyParameters = rawParams
        )
    }

    private fun parseBand(value: String): EqBand? {
        val parts = value.split(',', ':', ';').map { it.trim() }
        return if (parts.size >= 2) {
            val freq = parseIntSafe(parts[0], 1000)
            val gain = parseFloatSafe(parts[1], 0f)
            val q = if (parts.size >= 3) parseFloatSafe(parts[2], 1.0f) else 1.0f
            EqBand(freq, gain, q)
        } else if (parts.size == 1) {
            val gain = parseFloatSafe(parts[0], 0f)
            EqBand(1000, gain)
        } else null
    }

    private fun parseGainHexOrDec(value: String): Float {
        return try {
            if (value.startsWith("0x", ignoreCase = true)) {
                val hex = value.substring(2)
                (hex.toInt(16) - 8).toFloat()
            } else {
                value.toFloat()
            }
        } catch (_: Exception) {
            0f
        }
    }

    private fun parseFloatSafe(value: String, default: Float): Float {
        return value.replace("db", "", ignoreCase = true).trim().toFloatOrNull() ?: default
    }

    private fun parseIntSafe(value: String, default: Int): Int {
        return value.replace("hz", "", ignoreCase = true).trim().toIntOrNull() ?: default
    }

    private fun parseBoolean(value: String): Boolean {
        return when (value.lowercase()) {
            "1", "true", "yes", "on", "enable", "enabled" -> true
            else -> false
        }
    }
}
