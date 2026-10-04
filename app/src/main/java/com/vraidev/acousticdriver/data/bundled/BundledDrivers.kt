package com.vraidev.acousticdriver.data.bundled

import com.vraidev.acousticdriver.core.air.*

object BundledDrivers {
    val catalog: List<AcousticIntermediateRepresentation> = listOf(
        AcousticIntermediateRepresentation(
            metadata = DriverMetadata(
                id = "se-w810-mega-acoustic",
                name = "Mega Acoustic",
                author = "Black_Gohan & Community",
                sourceDevice = "Sony Ericsson W810i",
                version = "2.4",
                description = "Legendary Walkman acoustic tuning. Deep sub-bass extension, punchy transients, and sparkling highs tuned for earphones and external speakers.",
                tags = listOf("Sony Ericsson", "Bass", "Walkman", "Classic"),
                year = "2006"
            ),
            preampDb = -1.5f,
            masterGainDb = 2.0f,
            graphicEq = listOf(
                EqBand(60, 5.0f, 0.8f),
                EqBand(150, 3.5f, 0.9f),
                EqBand(400, -0.5f, 1.0f),
                EqBand(1000, 0.0f, 1.0f),
                EqBand(2500, 1.5f, 1.1f),
                EqBand(6000, 3.0f, 0.9f),
                EqBand(14000, 4.5f, 0.7f)
            ),
            bassBoost = BassConfig(enabled = true, strength = 750, cutoffHz = 90, boostDb = 6.0f),
            compressor = CompressorConfig(enabled = true, thresholdDb = -14.0f, ratio = 2.8f, attackMs = 8f, releaseMs = 90f),
            limiter = LimiterConfig(enabled = true, ceilingDb = -0.5f),
            stereoSpatial = SpatialConfig(enabled = true, strength = 300, stereoWidthFactor = 1.2f),
            rawLegacyParameters = mapOf(
                "Ear_Gain" to "0x0A",
                "Spk_Gain" to "0x0C",
                "MegaBass" to "Enabled",
                "DynamicRangeControl" to "Active"
            )
        ),

        AcousticIntermediateRepresentation(
            metadata = DriverMetadata(
                id = "se-k750-clear-bass",
                name = "Clear Bass Master",
                author = "K750i Mod Group",
                sourceDevice = "Sony Ericsson K750i / W800i",
                version = "3.1",
                description = "Focused sub-bass with zero distortion on acoustic phone transducers. Bright presence band for crystalline vocals.",
                tags = listOf("Sony Ericsson", "Clarity", "Bass", "Vocal"),
                year = "2005"
            ),
            preampDb = -2.0f,
            masterGainDb = 1.0f,
            graphicEq = listOf(
                EqBand(60, 4.0f, 0.9f),
                EqBand(150, 1.5f, 1.0f),
                EqBand(500, -1.0f, 1.2f),
                EqBand(1200, 1.0f, 1.0f),
                EqBand(3000, 2.5f, 1.0f),
                EqBand(8000, 3.0f, 0.8f),
                EqBand(15000, 2.0f, 0.7f)
            ),
            bassBoost = BassConfig(enabled = true, strength = 600, cutoffHz = 80, boostDb = 4.5f),
            compressor = CompressorConfig(enabled = true, thresholdDb = -12.0f, ratio = 2.2f, attackMs = 12f, releaseMs = 110f),
            limiter = LimiterConfig(enabled = true, ceilingDb = -0.3f),
            stereoSpatial = SpatialConfig(enabled = false, strength = 0),
            rawLegacyParameters = mapOf(
                "Ear_Gain" to "0x08",
                "Spk_Gain" to "0x09",
                "ClearBass" to "1"
            )
        ),

        AcousticIntermediateRepresentation(
            metadata = DriverMetadata(
                id = "se-w995-soundstage-x",
                name = "Walkman Soundstage X",
                author = "Cybershot & Walkman Labs",
                sourceDevice = "Sony Ericsson W995",
                version = "1.8",
                description = "Flagship stereo widening profile. Creates an expansive 3D sound field with tight bass and transparent instrument separation.",
                tags = listOf("Sony Ericsson", "Walkman", "Spatial", "Hi-Fi"),
                year = "2009"
            ),
            preampDb = -1.0f,
            masterGainDb = 1.5f,
            graphicEq = listOf(
                EqBand(80, 2.5f, 0.7f),
                EqBand(200, 1.0f, 0.9f),
                EqBand(800, 0.0f, 1.0f),
                EqBand(2000, 2.0f, 1.0f),
                EqBand(5000, 3.5f, 0.9f),
                EqBand(12000, 4.0f, 0.8f)
            ),
            bassBoost = BassConfig(enabled = true, strength = 450, cutoffHz = 110, boostDb = 3.5f),
            compressor = CompressorConfig(enabled = true, thresholdDb = -10.0f, ratio = 1.8f, attackMs = 15f, releaseMs = 150f),
            limiter = LimiterConfig(enabled = true, ceilingDb = -0.2f),
            stereoSpatial = SpatialConfig(enabled = true, strength = 700, stereoWidthFactor = 1.5f),
            rawLegacyParameters = mapOf(
                "Spatial_Stereo" to "Active",
                "ClearAudio_Experience" to "1"
            )
        ),

        AcousticIntermediateRepresentation(
            metadata = DriverMetadata(
                id = "audiophile-linear-ref",
                name = "Audiophile Linear Reference",
                author = "Vrai-Dev Acoustic Lab",
                sourceDevice = "Studio Reference Monitor",
                version = "1.0",
                description = "Flat, neutral frequency response calibrated for critical listening, IEMs, and high-fidelity USB DACs.",
                tags = listOf("Hi-Fi", "Studio", "Linear", "Reference"),
                year = "2024"
            ),
            preampDb = 0.0f,
            masterGainDb = 0.0f,
            graphicEq = listOf(
                EqBand(60, 0.0f),
                EqBand(230, 0.0f),
                EqBand(910, 0.0f),
                EqBand(3600, 0.0f),
                EqBand(14000, 0.0f)
            ),
            bassBoost = null,
            compressor = null,
            limiter = LimiterConfig(enabled = true, ceilingDb = -0.1f),
            stereoSpatial = null,
            rawLegacyParameters = mapOf("Linear_Mode" to "True")
        ),

        AcousticIntermediateRepresentation(
            metadata = DriverMetadata(
                id = "club-loudness-punch",
                name = "Club Loudness & Punch",
                author = "Basshead Community",
                sourceDevice = "Club Sound Rig",
                version = "2.0",
                description = "Maximum energy tuning engineered for party speakers and outdoor listening with heavy bass warmth and limiter protection.",
                tags = listOf("Bass", "Loudness", "Party", "Speaker"),
                year = "2010"
            ),
            preampDb = -3.0f,
            masterGainDb = 3.5f,
            graphicEq = listOf(
                EqBand(50, 6.0f),
                EqBand(120, 5.0f),
                EqBand(300, 2.0f),
                EqBand(1000, -1.0f),
                EqBand(3000, 1.5f),
                EqBand(8000, 4.0f),
                EqBand(16000, 5.0f)
            ),
            bassBoost = BassConfig(enabled = true, strength = 900, cutoffHz = 85, boostDb = 8.0f),
            compressor = CompressorConfig(enabled = true, thresholdDb = -16.0f, ratio = 3.5f, attackMs = 5f, releaseMs = 70f),
            limiter = LimiterConfig(enabled = true, ceilingDb = -0.1f),
            stereoSpatial = SpatialConfig(enabled = true, strength = 400),
            rawLegacyParameters = mapOf("Loudness_Boost" to "Max")
        ),

        AcousticIntermediateRepresentation(
            metadata = DriverMetadata(
                id = "vocal-clarity-boost",
                name = "Vocal Clarity & Podcast",
                author = "Speech Tuning Labs",
                sourceDevice = "Broadcast Studio",
                version = "1.2",
                description = "Attenuates boomy low frequencies while boosting voice fundamentals (1kHz - 4kHz) for crisp dialogues and podcasts.",
                tags = listOf("Vocal", "Podcast", "Speech", "Clarity"),
                year = "2011"
            ),
            preampDb = -0.5f,
            masterGainDb = 1.0f,
            graphicEq = listOf(
                EqBand(80, -3.0f),
                EqBand(200, -1.0f),
                EqBand(1000, 3.0f),
                EqBand(2500, 4.0f),
                EqBand(5000, 2.5f),
                EqBand(10000, 0.5f)
            ),
            bassBoost = null,
            compressor = CompressorConfig(enabled = true, thresholdDb = -18.0f, ratio = 2.5f, attackMs = 10f, releaseMs = 120f),
            limiter = LimiterConfig(enabled = true, ceilingDb = -0.5f),
            stereoSpatial = null,
            rawLegacyParameters = mapOf("Speech_Enhancer" to "1")
        ),

        AcousticIntermediateRepresentation(
            metadata = DriverMetadata(
                id = "se-tosha-acoustic-v2",
                name = "ToSha Acoustic v2.0 Final",
                author = "ToSha (SE-NSE Community)",
                sourceDevice = "Sony Ericsson W800i / K750i",
                version = "2.0",
                description = "Legendary earbud driver tailored for HPM-70 earphones. Warm, velvety low-end with smooth, fatigue-free highs and lifelike timbre.",
                tags = listOf("Sony Ericsson", "Walkman", "Bass", "Classic"),
                year = "2006"
            ),
            preampDb = -1.0f,
            masterGainDb = 2.0f,
            graphicEq = listOf(
                EqBand(60, 5.5f, 0.8f),
                EqBand(150, 4.0f, 0.9f),
                EqBand(400, 0.5f, 1.0f),
                EqBand(1000, 0.0f, 1.0f),
                EqBand(2500, 1.0f, 1.1f),
                EqBand(6000, 2.5f, 0.9f),
                EqBand(14000, 3.5f, 0.7f)
            ),
            bassBoost = BassConfig(enabled = true, strength = 700, cutoffHz = 75, boostDb = 5.5f),
            compressor = CompressorConfig(enabled = true, thresholdDb = -13.0f, ratio = 2.4f, attackMs = 12f, releaseMs = 100f),
            limiter = LimiterConfig(enabled = true, ceilingDb = -0.4f),
            stereoSpatial = SpatialConfig(enabled = true, strength = 250, stereoWidthFactor = 1.15f),
            rawLegacyParameters = mapOf(
                "Ear_Gain" to "0x0B",
                "Spk_Gain" to "0x0D",
                "MegaBass" to "Enabled",
                "Modder" to "ToSha"
            )
        ),

        AcousticIntermediateRepresentation(
            metadata = DriverMetadata(
                id = "se-kryak-loudness-v22",
                name = "Kryak Extreme Loudness v2.2",
                author = "Kryak",
                sourceDevice = "Sony Ericsson W810i / W580i",
                version = "2.2",
                description = "The most celebrated speaker mod on TopSony and Esato. Pushes output volume to the absolute ceiling while maintaining dynamic peak safety.",
                tags = listOf("Sony Ericsson", "Loudness", "Speaker", "Bass"),
                year = "2007"
            ),
            preampDb = -2.5f,
            masterGainDb = 4.0f,
            graphicEq = listOf(
                EqBand(60, 4.0f, 0.8f),
                EqBand(150, 5.0f, 0.9f),
                EqBand(400, 1.5f, 1.0f),
                EqBand(1000, -0.5f, 1.0f),
                EqBand(2500, 2.0f, 1.0f),
                EqBand(6000, 4.5f, 0.8f),
                EqBand(14000, 5.0f, 0.7f)
            ),
            bassBoost = BassConfig(enabled = true, strength = 850, cutoffHz = 90, boostDb = 7.0f),
            compressor = CompressorConfig(enabled = true, thresholdDb = -15.0f, ratio = 3.2f, attackMs = 6f, releaseMs = 80f),
            limiter = LimiterConfig(enabled = true, ceilingDb = -0.2f),
            stereoSpatial = SpatialConfig(enabled = true, strength = 350),
            rawLegacyParameters = mapOf(
                "Spk_Gain" to "0x0F",
                "Loudness" to "Maximum",
                "Acoustic_Revision" to "Kryak_v2.2"
            )
        ),

        AcousticIntermediateRepresentation(
            metadata = DriverMetadata(
                id = "se-peter-audiophile-v4",
                name = "Peter Acoustic v4.0 Final",
                author = "Peter (Esato)",
                sourceDevice = "Sony Ericsson W900i / W910i",
                version = "4.0",
                description = "Audiophile tuning engineered for pristine instrument separation and wide, airy soundstaging with zero artificial harshness.",
                tags = listOf("Sony Ericsson", "Walkman", "Hi-Fi", "Spatial"),
                year = "2007"
            ),
            preampDb = -1.0f,
            masterGainDb = 1.0f,
            graphicEq = listOf(
                EqBand(60, 3.0f, 0.9f),
                EqBand(150, 1.5f, 1.0f),
                EqBand(400, 0.0f, 1.0f),
                EqBand(1000, 0.5f, 1.0f),
                EqBand(2500, 2.0f, 1.0f),
                EqBand(6000, 3.5f, 0.9f),
                EqBand(14000, 4.5f, 0.8f)
            ),
            bassBoost = BassConfig(enabled = true, strength = 500, cutoffHz = 65, boostDb = 4.0f),
            compressor = CompressorConfig(enabled = true, thresholdDb = -11.0f, ratio = 1.8f, attackMs = 15f, releaseMs = 140f),
            limiter = LimiterConfig(enabled = true, ceilingDb = -0.1f),
            stereoSpatial = SpatialConfig(enabled = true, strength = 550, stereoWidthFactor = 1.35f),
            rawLegacyParameters = mapOf(
                "Audio_DAC" to "Hi-Fi",
                "StereoWidening" to "Enabled",
                "Tuner" to "Peter_Final"
            )
        ),

        AcousticIntermediateRepresentation(
            metadata = DriverMetadata(
                id = "se-k800-cybershot-studio",
                name = "Cyber-shot Studio Acoustic",
                author = "Cyber-shot Tuning Team",
                sourceDevice = "Sony Ericsson K800i / K850i",
                version = "1.5",
                description = "Authentic Sony Cyber-shot acoustic voicing. Tight transient dynamics, focused mid-clarity for speech and vocals, and crisp high presence.",
                tags = listOf("Sony Ericsson", "Cyber-shot", "Clarity", "Studio"),
                year = "2006"
            ),
            preampDb = -0.5f,
            masterGainDb = 1.5f,
            graphicEq = listOf(
                EqBand(60, 2.0f, 0.9f),
                EqBand(150, 1.0f, 1.0f),
                EqBand(400, -0.5f, 1.0f),
                EqBand(1000, 1.5f, 1.1f),
                EqBand(2500, 3.0f, 1.1f),
                EqBand(6000, 2.5f, 0.9f),
                EqBand(14000, 2.0f, 0.8f)
            ),
            bassBoost = BassConfig(enabled = true, strength = 400, cutoffHz = 90, boostDb = 3.0f),
            compressor = CompressorConfig(enabled = true, thresholdDb = -12.0f, ratio = 2.0f, attackMs = 10f, releaseMs = 110f),
            limiter = LimiterConfig(enabled = true, ceilingDb = -0.4f),
            stereoSpatial = null,
            rawLegacyParameters = mapOf(
                "Series" to "Cyber-shot",
                "Sensor_Mode" to "Dynamic_Stereo"
            )
        ),

        AcousticIntermediateRepresentation(
            metadata = DriverMetadata(
                id = "se-walkman-megabass-db2020",
                name = "Walkman 2.0 MegaBass Factory",
                author = "Sony Ericsson Walkman Team",
                sourceDevice = "Sony Ericsson W850i / W880i",
                version = "2.0",
                description = "The authentic factory DB2020 Walkman 2.0 MegaBass acoustic profile. The definitive nostalgic bass curve of mid-2000s portable audio.",
                tags = listOf("Sony Ericsson", "Walkman", "Bass", "MegaBass"),
                year = "2006"
            ),
            preampDb = -1.5f,
            masterGainDb = 1.0f,
            graphicEq = listOf(
                EqBand(60, 6.5f, 0.8f),
                EqBand(250, 2.5f, 1.0f),
                EqBand(1000, 0.0f, 1.0f),
                EqBand(4000, 2.0f, 1.0f),
                EqBand(16000, 4.0f, 0.7f)
            ),
            bassBoost = BassConfig(enabled = true, strength = 800, cutoffHz = 60, boostDb = 6.5f),
            compressor = CompressorConfig(enabled = true, thresholdDb = -13.0f, ratio = 2.6f, attackMs = 8f, releaseMs = 95f),
            limiter = LimiterConfig(enabled = true, ceilingDb = -0.3f),
            stereoSpatial = SpatialConfig(enabled = true, strength = 200),
            rawLegacyParameters = mapOf(
                "Firmware_Profile" to "DB2020_W850_R1GB001",
                "MegaBass_Algorithm" to "TrueHardwareDSP"
            )
        ),

        AcousticIntermediateRepresentation(
            metadata = DriverMetadata(
                id = "se-w902-clearaudio-hifi",
                name = "Zero Distortion Hi-Fi (W902)",
                author = "Sony ClearAudio Labs",
                sourceDevice = "Sony Ericsson W902",
                version = "3.0",
                description = "Tuned after the flagship W902 with dedicated hardware DAC. Crystal clear bass definition, transparent mids, and zero acoustic distortion.",
                tags = listOf("Sony Ericsson", "Walkman", "Hi-Fi", "Reference"),
                year = "2008"
            ),
            preampDb = 0.0f,
            masterGainDb = 0.5f,
            graphicEq = listOf(
                EqBand(60, 2.5f, 0.9f),
                EqBand(150, 1.0f, 1.0f),
                EqBand(400, 0.0f, 1.0f),
                EqBand(1000, 0.0f, 1.0f),
                EqBand(2500, 1.0f, 1.0f),
                EqBand(6000, 2.0f, 0.9f),
                EqBand(14000, 2.5f, 0.8f)
            ),
            bassBoost = BassConfig(enabled = true, strength = 350, cutoffHz = 70, boostDb = 3.0f),
            compressor = CompressorConfig(enabled = true, thresholdDb = -10.0f, ratio = 1.5f, attackMs = 18f, releaseMs = 160f),
            limiter = LimiterConfig(enabled = true, ceilingDb = -0.1f),
            stereoSpatial = SpatialConfig(enabled = true, strength = 350, stereoWidthFactor = 1.2f),
            rawLegacyParameters = mapOf(
                "Hardware_DAC" to "Wolfson_WM8978",
                "ClearAudio_Experience" to "True"
            )
        ),

        AcousticIntermediateRepresentation(
            metadata = DriverMetadata(
                id = "se-blackgohan-pure-v41",
                name = "Pure Acoustic v4.1",
                author = "Black_Gohan",
                sourceDevice = "Sony Ericsson W810i",
                version = "4.1",
                description = "Black_Gohan's magnum opus. Combines gut-punching low end with sparkling Walkman treble, perfect for IEMs, headphones, and external speakers.",
                tags = listOf("Sony Ericsson", "Walkman", "Bass", "Loudness"),
                year = "2007"
            ),
            preampDb = -2.0f,
            masterGainDb = 2.5f,
            graphicEq = listOf(
                EqBand(60, 6.0f, 0.8f),
                EqBand(150, 4.0f, 0.9f),
                EqBand(400, 1.0f, 1.0f),
                EqBand(1000, 0.0f, 1.0f),
                EqBand(2500, 1.5f, 1.1f),
                EqBand(6000, 3.5f, 0.9f),
                EqBand(14000, 5.0f, 0.7f)
            ),
            bassBoost = BassConfig(enabled = true, strength = 850, cutoffHz = 80, boostDb = 6.5f),
            compressor = CompressorConfig(enabled = true, thresholdDb = -14.0f, ratio = 2.5f, attackMs = 7f, releaseMs = 85f),
            limiter = LimiterConfig(enabled = true, ceilingDb = -0.3f),
            stereoSpatial = SpatialConfig(enabled = true, strength = 300, stereoWidthFactor = 1.25f),
            rawLegacyParameters = mapOf(
                "Author" to "Black_Gohan",
                "Driver_Series" to "Pure_Acoustic_4.1",
                "Bass_Cutoff" to "80Hz"
            )
        )
    )
}
