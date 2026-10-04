package com.vraidev.acousticdriver.core.translator

import com.vraidev.acousticdriver.audio.model.DeviceCapability
import com.vraidev.acousticdriver.core.air.*

class AcousticTranslator {

    fun analyzeCompatibility(
        air: AcousticIntermediateRepresentation,
        capability: DeviceCapability
    ): CompatibilityScore {
        val evaluations = mutableListOf<ParameterEvaluation>()

        // 1. Equalizer Analysis
        if (air.graphicEq.isNotEmpty()) {
            if (capability.hasEqualizer) {
                val bandCount = air.graphicEq.size
                if (bandCount <= capability.equalizerBands) {
                    evaluations.add(
                        ParameterEvaluation(
                            parameterName = "Graphic Equalizer ($bandCount bands)",
                            sourceValue = "${air.graphicEq.first().frequencyHz}Hz to ${air.graphicEq.last().frequencyHz}Hz",
                            targetEffect = "android.media.audiofx.Equalizer",
                            status = ParameterClassification.NATIVE,
                            explanation = "Native hardware-accelerated equalizer supports all $bandCount legacy bands."
                        )
                    )
                } else {
                    evaluations.add(
                        ParameterEvaluation(
                            parameterName = "Graphic Equalizer ($bandCount bands)",
                            sourceValue = "$bandCount bands",
                            targetEffect = "android.media.audiofx.Equalizer (${capability.equalizerBands} bands)",
                            status = ParameterClassification.APPROXIMATED,
                            explanation = "Downsampled from $bandCount legacy bands to ${capability.equalizerBands} device bands via spline interpolation."
                        )
                    )
                }
            } else {
                evaluations.add(
                    ParameterEvaluation(
                        parameterName = "Graphic Equalizer",
                        sourceValue = "${air.graphicEq.size} bands",
                        targetEffect = "Equalizer",
                        status = ParameterClassification.UNSUPPORTED,
                        explanation = "Device reports no available Equalizer audio effect."
                    )
                )
            }
        }

        // 2. Bass Enhancement / MegaBass Analysis
        if (air.bassBoost != null && air.bassBoost.enabled) {
            if (capability.hasBassBoost) {
                evaluations.add(
                    ParameterEvaluation(
                        parameterName = "MegaBass / Bass Enhancement",
                        sourceValue = "Strength: ${air.bassBoost.strength}/1000",
                        targetEffect = "android.media.audiofx.BassBoost",
                        status = ParameterClassification.NATIVE,
                        explanation = "Mapped to Android BassBoost with dynamic clipping headroom safeguard."
                    )
                )
            } else {
                evaluations.add(
                    ParameterEvaluation(
                        parameterName = "MegaBass",
                        sourceValue = "Strength: ${air.bassBoost.strength}",
                        targetEffect = "Low-Shelf Equalizer",
                        status = ParameterClassification.APPROXIMATED,
                        explanation = "Native BassBoost missing; approximated using low-frequency EQ shelf."
                    )
                )
            }
        }

        // 3. Dynamics / Multiband Compressor
        if (air.compressor != null && air.compressor.enabled) {
            if (capability.hasDynamicsProcessing) {
                evaluations.add(
                    ParameterEvaluation(
                        parameterName = "Dynamic Range Compressor",
                        sourceValue = "Thresh: ${air.compressor.thresholdDb}dB, Ratio: ${air.compressor.ratio}:1",
                        targetEffect = "android.media.audiofx.DynamicsProcessing.Mbc",
                        status = ParameterClassification.NATIVE,
                        explanation = "Native multiband dynamic compression supported via Android DynamicsProcessing API."
                    )
                )
            } else {
                evaluations.add(
                    ParameterEvaluation(
                        parameterName = "Dynamic Range Compressor",
                        sourceValue = "Thresh: ${air.compressor.thresholdDb}dB",
                        targetEffect = "Software Lookahead Peak Limiter",
                        status = ParameterClassification.APPROXIMATED,
                        explanation = "DynamicsProcessing unavailable; simulated via gain leveling and peak ceiling."
                    )
                )
            }
        }

        // 4. Limiter / Output Ceiling
        if (air.limiter != null && air.limiter.enabled) {
            if (capability.hasDynamicsProcessing) {
                evaluations.add(
                    ParameterEvaluation(
                        parameterName = "Acoustic Output Limiter",
                        sourceValue = "Ceiling: ${air.limiter.ceilingDb}dB",
                        targetEffect = "DynamicsProcessing.Limiter",
                        status = ParameterClassification.NATIVE,
                        explanation = "Prevents speaker distortion and acoustic clipping."
                    )
                )
            } else {
                evaluations.add(
                    ParameterEvaluation(
                        parameterName = "Acoustic Output Limiter",
                        sourceValue = "Ceiling: ${air.limiter.ceilingDb}dB",
                        targetEffect = "Preamp Attenuation",
                        status = ParameterClassification.APPROXIMATED,
                        explanation = "Approximated by lowering master preamp to provide headroom."
                    )
                )
            }
        }

        // 5. Hardware Gain & Amp Calibration
        if (air.rawLegacyParameters.any { it.key.contains("speaker_gain") || it.key.contains("hardware") }) {
            evaluations.add(
                ParameterEvaluation(
                    parameterName = "Hardware Amplifier Gain",
                    sourceValue = "Legacy Gain Code",
                    targetEffect = "Software Preamp & LoudnessEnhancer",
                    status = ParameterClassification.APPROXIMATED,
                    explanation = "Standard Android Audio: translated into calibrated digital preamp headroom."
                )
            )
        }

        // 6. Unknown / Proprietary DSP filter coefficients
        val unknownCount = air.rawLegacyParameters.count {
            it.key.contains("register") || it.key.contains("coeff") || it.key.contains("calibration")
        }
        if (unknownCount > 0) {
            evaluations.add(
                ParameterEvaluation(
                    parameterName = "Proprietary DSP Calibration ($unknownCount items)",
                    sourceValue = "$unknownCount registers",
                    targetEffect = "Ignored / Sandbox",
                    status = ParameterClassification.UNSUPPORTED,
                    explanation = "Proprietary Sony Ericsson ASIC registers cannot be emulated on Android without vendor DSP firmware."
                )
            )
        }

        var totalWeight = 0f
        var earnedWeight = 0f
        var nativeCount = 0
        var approxCount = 0
        var unsuppCount = 0

        for (eval in evaluations) {
            totalWeight += 1.0f
            earnedWeight += eval.status.weight
            when (eval.status) {
                ParameterClassification.EXACT, ParameterClassification.NATIVE -> nativeCount++
                ParameterClassification.APPROXIMATED, ParameterClassification.PARTIAL -> approxCount++
                ParameterClassification.UNSUPPORTED, ParameterClassification.UNKNOWN -> unsuppCount++
            }
        }

        val percentage = if (totalWeight > 0f) {
            ((earnedWeight / totalWeight) * 100).toInt().coerceIn(10, 100)
        } else {
            100
        }

        return CompatibilityScore(
            percentage = percentage,
            evaluations = evaluations,
            nativeCount = nativeCount,
            approximatedCount = approxCount,
            unsupportedCount = unsuppCount
        )
    }

    /**
     * Map arbitrary AIR EQ bands to the device's actual available bands
     * using smooth logarithmic (octave-linear) frequency response interpolation.
     */
    fun mapEqBandsToDevice(
        sourceBands: List<EqBand>,
        deviceBandFrequencies: List<Int>
    ): List<Float> {
        if (deviceBandFrequencies.isEmpty()) return emptyList()
        if (sourceBands.isEmpty()) return deviceBandFrequencies.map { 0f }

        val sortedSource = sourceBands.sortedBy { it.frequencyHz }

        return deviceBandFrequencies.map { targetFreq ->
            interpolateGainAtFrequency(sortedSource, targetFreq)
        }
    }

    private fun interpolateGainAtFrequency(sortedSource: List<EqBand>, targetFreq: Int): Float {
        if (sortedSource.isEmpty()) return 0f
        if (targetFreq <= sortedSource.first().frequencyHz) {
            return sortedSource.first().gainDb
        }
        if (targetFreq >= sortedSource.last().frequencyHz) {
            return sortedSource.last().gainDb
        }

        for (i in 0 until sortedSource.size - 1) {
            val b1 = sortedSource[i]
            val b2 = sortedSource[i + 1]
            if (targetFreq in b1.frequencyHz..b2.frequencyHz) {
                if (b1.frequencyHz == b2.frequencyHz) return b1.gainDb
                val logF1 = Math.log(b1.frequencyHz.toDouble())
                val logF2 = Math.log(b2.frequencyHz.toDouble())
                val logT = Math.log(targetFreq.toDouble())
                val t = (logT - logF1) / (logF2 - logF1)
                return (b1.gainDb + t * (b2.gainDb - b1.gainDb)).toFloat()
            }
        }
        return 0f
    }
}
