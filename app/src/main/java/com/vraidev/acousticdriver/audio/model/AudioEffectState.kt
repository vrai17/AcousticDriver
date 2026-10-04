package com.vraidev.acousticdriver.audio.model

import com.vraidev.acousticdriver.core.air.AcousticIntermediateRepresentation

enum class VocalTargetMode(val label: String, val frequencyLabel: String, val description: String) {
    BALANCED("Balanced", "2.0 – 2.2 kHz", "Universal vocal presence & clarity for most genres"),
    CRISP_SPEECH("Crisp / Speech", "2.5 – 3.2 kHz", "Optimized for podcasts, female vocals & audiobooks"),
    WARM_MALE("Warmth / Male", "1.2 – 1.8 kHz", "Deeper chest resonance & fundamental baritone warmth")
}

enum class VocalFocus(val label: String, val description: String) {
    SURGICAL("Surgical (Anti-Bleed)", "Tight formant Q with instrument de-masking"),
    NATURAL("Natural", "Gentle musical curve across upper-mids")
}

data class AudioEffectState(
    val activeDriver: AcousticIntermediateRepresentation? = null,
    val isEnabled: Boolean = true,
    val isBypassActive: Boolean = false, // For A/B testing
    val volumeMatchEnabled: Boolean = true,
    val volumeCompensationDb: Float = 0f,
    val preampDb: Float = 0f,
    val bassBoostStrength: Int = 0, // 0 - 1000
    val bassBoostEnabled: Boolean = false,
    val vocalBoosterEnabled: Boolean = false,
    val vocalBoosterStrength: Int = 50, // 0 - 100%
    val vocalTargetMode: VocalTargetMode = VocalTargetMode.BALANCED,
    val vocalIsolationFocus: VocalFocus = VocalFocus.SURGICAL,
    val virtualizerStrength: Int = 0, // 0 - 1000
    val virtualizerEnabled: Boolean = false,
    val dynamicsEnabled: Boolean = false,
    val compressorThresholdDb: Float = -12f,
    val compressorRatio: Float = 2.0f,
    val limiterCeilingDb: Float = -0.5f,
    val eqBandGains: List<Float> = emptyList(), // in dB
    val currentOutput: OutputDevice = OutputDevice.PHONE_SPEAKER
)
