package com.vraidev.acousticdriver.audio

import android.content.Context
import android.media.audiofx.AudioEffect
import android.media.audiofx.Equalizer
import android.os.Build
import com.vraidev.acousticdriver.audio.model.DeviceCapability
import com.vraidev.acousticdriver.audio.model.DiscoveredEffect
import java.io.File

class DeviceCapabilityScanner(private val context: Context) {

    fun scanCapabilities(): DeviceCapability {
        val discoveredList = mutableListOf<DiscoveredEffect>()
        var hasEq = false
        var eqBands = 5
        val bandFrequencies = mutableListOf<Int>()
        var hasBass = false
        var hasVirt = false
        var hasDynamics = false
        var hasLoudness = false

        // 1. Query System Audio Effects
        try {
            val descriptors = AudioEffect.queryEffects()
            if (descriptors != null) {
                for (desc in descriptors) {
                    discoveredList.add(
                        DiscoveredEffect(
                            type = desc.type.toString(),
                            name = desc.name ?: "Unknown Effect",
                            implementor = desc.implementor ?: "Vendor",
                            uuid = desc.uuid.toString()
                        )
                    )
                    when (desc.type) {
                        AudioEffect.EFFECT_TYPE_EQUALIZER -> hasEq = true
                        AudioEffect.EFFECT_TYPE_BASS_BOOST -> hasBass = true
                        AudioEffect.EFFECT_TYPE_VIRTUALIZER -> hasVirt = true
                        AudioEffect.EFFECT_TYPE_DYNAMICS_PROCESSING -> hasDynamics = true
                        AudioEffect.EFFECT_TYPE_LOUDNESS_ENHANCER -> hasLoudness = true
                    }
                }
            }
        } catch (_: Exception) {}

        // 2. Query Equalizer details via temporary test instance
        var nativeBands = 5
        val nativeBandFrequencies = mutableListOf<Int>()
        try {
            val tempEq = Equalizer(0, 0)
            hasEq = true
            nativeBands = tempEq.numberOfBands.toInt()
            for (i in 0 until nativeBands) {
                nativeBandFrequencies.add(tempEq.getCenterFreq(i.toShort()) / 1000) // Convert mHz to Hz
            }
            tempEq.release()
        } catch (_: Exception) {
            // Default 5-band fallback frequencies
            if (nativeBandFrequencies.isEmpty()) {
                nativeBandFrequencies.addAll(listOf(60, 230, 910, 3600, 14000))
            }
        }

        // DynamicsProcessing is available natively in Android 9+ (API 28)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            hasDynamics = true
        }

        val isModern = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
        val standard10Frequencies = listOf(31, 62, 125, 250, 500, 1000, 2000, 4000, 8000, 16000)

        eqBands = if (isModern) standard10Frequencies.size else nativeBands
        bandFrequencies.clear()
        bandFrequencies.addAll(if (isModern) standard10Frequencies else nativeBandFrequencies)

        val halType = if (Build.VERSION.SDK_INT >= 34) {
            "AIDL Audio HAL"
        } else if (Build.VERSION.SDK_INT >= 29) {
            "HIDL Audio HAL"
        } else {
            "Legacy Audio HAL"
        }

        return DeviceCapability(
            manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
            model = Build.MODEL,
            soc = Build.HARDWARE,
            androidVersion = Build.VERSION.SDK_INT,
            audioHalType = halType,
            hasEqualizer = hasEq,
            equalizerBands = eqBands,
            bandFrequenciesHz = bandFrequencies,
            hasBassBoost = hasBass,
            hasVirtualizer = hasVirt,
            hasDynamicsProcessing = hasDynamics,
            hasLoudnessEnhancer = hasLoudness,
            isRooted = false,
            rootLevel = 0,
            discoveredEffects = discoveredList
        )
    }
}
