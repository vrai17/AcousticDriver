package com.vraidev.acousticdriver.audio.model

data class DiscoveredEffect(
    val type: String,
    val name: String,
    val implementor: String,
    val uuid: String
)

data class DeviceCapability(
    val manufacturer: String,
    val model: String,
    val soc: String,
    val androidVersion: Int,
    val audioHalType: String, // "AIDL", "HIDL", "Legacy"
    val hasEqualizer: Boolean,
    val equalizerBands: Int,
    val bandFrequenciesHz: List<Int>,
    val hasBassBoost: Boolean,
    val hasVirtualizer: Boolean,
    val hasDynamicsProcessing: Boolean,
    val hasLoudnessEnhancer: Boolean,
    val isRooted: Boolean,
    val rootLevel: Int, // 0 = No Root, 1 = Rooted, 2 = Config writable, 3 = HAL mixer, 4 = DSP, 5 = Vendor Deep
    val discoveredEffects: List<DiscoveredEffect>
) {
    companion object {
        fun defaultFallback(): DeviceCapability {
            val manufacturer = try {
                android.os.Build.MANUFACTURER?.replaceFirstChar { it.uppercase() } ?: "Generic"
            } catch (_: Exception) { "Generic" }

            val model = try { android.os.Build.MODEL ?: "Android Device" } catch (_: Exception) { "Android Device" }
            val soc = try { android.os.Build.HARDWARE ?: "ARM64" } catch (_: Exception) { "ARM64" }
            val sdkInt = try { android.os.Build.VERSION.SDK_INT } catch (_: Exception) { 34 }

            return DeviceCapability(
                manufacturer = manufacturer,
                model = model,
                soc = soc,
                androidVersion = sdkInt,
                audioHalType = if (sdkInt >= 34) "AIDL Audio HAL" else "HIDL Audio HAL",
                hasEqualizer = true,
                equalizerBands = 5,
                bandFrequenciesHz = listOf(60000, 230000, 910000, 3600000, 14000000).map { it / 1000 },
                hasBassBoost = true,
                hasVirtualizer = true,
                hasDynamicsProcessing = sdkInt >= 28,
                hasLoudnessEnhancer = true,
                isRooted = false,
                rootLevel = 0,
                discoveredEffects = emptyList()
            )
        }
    }
}
