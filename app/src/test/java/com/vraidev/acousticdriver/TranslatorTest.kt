package com.vraidev.acousticdriver

import com.vraidev.acousticdriver.audio.model.DeviceCapability
import com.vraidev.acousticdriver.core.air.*
import com.vraidev.acousticdriver.core.translator.AcousticTranslator
import org.junit.Assert.*
import org.junit.Test

class TranslatorTest {

    private val translator = AcousticTranslator()

    @Test
    fun testCompatibilityScoreCalculation() {
        val air = AcousticIntermediateRepresentation(
            metadata = DriverMetadata("test", "Test Profile", "Tester", "W810i", "1.0", "Desc"),
            graphicEq = listOf(
                EqBand(60, 4.0f),
                EqBand(230, 2.0f),
                EqBand(910, 0.0f),
                EqBand(3600, 2.0f),
                EqBand(14000, 3.5f)
            ),
            bassBoost = BassConfig(enabled = true, strength = 700),
            compressor = CompressorConfig(enabled = true, thresholdDb = -12f, ratio = 2f),
            limiter = LimiterConfig(enabled = true, ceilingDb = -0.5f),
            rawLegacyParameters = mapOf("speaker_gain" to "4")
        )

        // Test with No-Root capability
        val noRootCap = DeviceCapability(
            manufacturer = "Sony",
            model = "Xperia Test",
            soc = "Snapdragon",
            androidVersion = 34,
            audioHalType = "AIDL Audio HAL",
            hasEqualizer = true,
            equalizerBands = 5,
            bandFrequenciesHz = listOf(60, 230, 910, 3600, 14000),
            hasBassBoost = true,
            hasVirtualizer = true,
            hasDynamicsProcessing = true,
            hasLoudnessEnhancer = true,
            isRooted = false,
            rootLevel = 0,
            discoveredEffects = emptyList()
        )
        val noRootScore = translator.analyzeCompatibility(air, noRootCap)

        assertTrue(noRootScore.percentage in 70..95)
        assertTrue(noRootScore.nativeCount > 0)
        assertTrue(noRootScore.approximatedCount > 0)

        // Test with Root capability
        val rootCap = noRootCap.copy(isRooted = true, rootLevel = 4)
        val rootScore = translator.analyzeCompatibility(air, rootCap)

        assertTrue(rootScore.percentage >= noRootScore.percentage)
    }

    @Test
    fun testMapEqBandsToDevice() {
        val sourceBands = listOf(
            EqBand(60, 5.0f),
            EqBand(250, 3.0f),
            EqBand(1000, 0.0f),
            EqBand(4000, 2.0f),
            EqBand(16000, 4.0f)
        )
        val deviceFrequencies = listOf(60, 230, 910, 3600, 14000)

        val mapped = translator.mapEqBandsToDevice(sourceBands, deviceFrequencies)
        assertEquals(5, mapped.size)
        assertEquals(5.0f, mapped[0], 0.01f) // mapped to 60Hz
        assertEquals(3.0f, mapped[1], 0.2f) // interpolated close to 250Hz -> 230Hz
    }
}
