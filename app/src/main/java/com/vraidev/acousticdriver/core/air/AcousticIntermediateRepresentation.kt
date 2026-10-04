package com.vraidev.acousticdriver.core.air

import kotlinx.serialization.Serializable

@Serializable
data class DriverMetadata(
    val id: String,
    val name: String,
    val author: String,
    val sourceDevice: String,
    val version: String,
    val description: String,
    val tags: List<String> = emptyList(),
    val license: String = "Community / Free Use",
    val year: String = "2006-2010"
)

@Serializable
data class EqBand(
    val frequencyHz: Int,
    val gainDb: Float,
    val qFactor: Float = 1.0f
)

@Serializable
enum class FilterType {
    LOW_SHELF,
    HIGH_SHELF,
    PEAKING,
    LOW_PASS,
    HIGH_PASS,
    BAND_PASS,
    NOTCH
}

@Serializable
data class ParametricFilter(
    val type: FilterType,
    val frequencyHz: Float,
    val gainDb: Float,
    val qFactor: Float = 0.707f
)

@Serializable
data class BassConfig(
    val enabled: Boolean = true,
    val strength: Int = 500, // 0 to 1000
    val cutoffHz: Int = 100,
    val boostDb: Float = 6.0f
)

@Serializable
data class CompressorConfig(
    val enabled: Boolean = true,
    val thresholdDb: Float = -12.0f,
    val ratio: Float = 2.5f,
    val attackMs: Float = 10.0f,
    val releaseMs: Float = 120.0f,
    val makeupGainDb: Float = 2.0f
)

@Serializable
data class LimiterConfig(
    val enabled: Boolean = true,
    val ceilingDb: Float = -0.5f,
    val thresholdDb: Float = -1.0f,
    val attackMs: Float = 2.0f,
    val releaseMs: Float = 60.0f
)

@Serializable
data class SpatialConfig(
    val enabled: Boolean = false,
    val strength: Int = 0, // 0 to 1000
    val stereoWidthFactor: Float = 1.0f
)

@Serializable
data class AcousticIntermediateRepresentation(
    val metadata: DriverMetadata,
    val preampDb: Float = 0.0f,
    val masterGainDb: Float = 0.0f,
    val graphicEq: List<EqBand> = emptyList(),
    val parametricEq: List<ParametricFilter> = emptyList(),
    val bassBoost: BassConfig? = null,
    val compressor: CompressorConfig? = null,
    val limiter: LimiterConfig? = null,
    val stereoSpatial: SpatialConfig? = null,
    val rawLegacyParameters: Map<String, String> = emptyMap()
)
