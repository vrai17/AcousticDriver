package com.vraidev.acousticdriver.audio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.audiofx.*
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.vraidev.acousticdriver.audio.model.*
import com.vraidev.acousticdriver.core.air.AcousticIntermediateRepresentation
import com.vraidev.acousticdriver.core.translator.AcousticTranslator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AudioEngine(
    private val context: Context,
    private val capabilityScanner: DeviceCapabilityScanner,
    private val translator: AcousticTranslator
) {
    private val _state = MutableStateFlow(AudioEffectState())
    val state: StateFlow<AudioEffectState> = _state.asStateFlow()

    private val _capability = MutableStateFlow(DeviceCapability.defaultFallback())
    val capability: StateFlow<DeviceCapability> = _capability.asStateFlow()

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var audioDeviceCallback: AudioDeviceCallback? = null
    private var headsetReceiver: BroadcastReceiver? = null

    private var activeSessionId = 0
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var dynamicsProcessing: DynamicsProcessing? = null
    val standardFrequencies = listOf(31, 62, 125, 250, 500, 1000, 2000, 4000, 8000, 16000)

    fun initialize() {
        val detectedCap = capabilityScanner.scanCapabilities()
        _capability.value = detectedCap
        registerAudioDeviceObserver()
        bindAudioSession(0)
    }

    private fun detectCurrentOutputDevice(): OutputDevice {
        val am = audioManager ?: return OutputDevice.PHONE_SPEAKER

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val devices = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            val wired = devices.firstOrNull {
                it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                it.type == AudioDeviceInfo.TYPE_LINE_ANALOG ||
                it.type == AudioDeviceInfo.TYPE_LINE_DIGITAL
            }
            if (wired != null) return OutputDevice.WIRED_HEADSET

            val usb = devices.firstOrNull {
                it.type == AudioDeviceInfo.TYPE_USB_DEVICE ||
                it.type == AudioDeviceInfo.TYPE_USB_HEADSET ||
                it.type == AudioDeviceInfo.TYPE_USB_ACCESSORY
            }
            if (usb != null) return OutputDevice.USB_DAC

            val bt = devices.firstOrNull {
                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                it.type == AudioDeviceInfo.TYPE_HEARING_AID
            }
            if (bt != null) return OutputDevice.BLUETOOTH

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val ble = devices.firstOrNull {
                    it.type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
                    it.type == AudioDeviceInfo.TYPE_BLE_SPEAKER ||
                    it.type == AudioDeviceInfo.TYPE_BLE_BROADCAST
                }
                if (ble != null) return OutputDevice.BLUETOOTH_LE
            }
        } else {
            @Suppress("DEPRECATION")
            if (am.isWiredHeadsetOn) return OutputDevice.WIRED_HEADSET
            @Suppress("DEPRECATION")
            if (am.isBluetoothA2dpOn || am.isBluetoothScoOn) return OutputDevice.BLUETOOTH
        }

        return OutputDevice.PHONE_SPEAKER
    }

    private fun registerAudioDeviceObserver() {
        val updateCurrent = {
            val detected = detectCurrentOutputDevice()
            _state.update { it.copy(currentOutput = detected) }
        }

        updateCurrent()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val callback = object : AudioDeviceCallback() {
                override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
                    updateCurrent()
                }

                override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
                    updateCurrent()
                }
            }
            audioManager?.registerAudioDeviceCallback(callback, Handler(Looper.getMainLooper()))
            audioDeviceCallback = callback
        }

        try {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (intent?.action == Intent.ACTION_HEADSET_PLUG) {
                        updateCurrent()
                    }
                }
            }
            context.registerReceiver(receiver, IntentFilter(Intent.ACTION_HEADSET_PLUG))
            headsetReceiver = receiver
        } catch (_: Exception) {}
    }

    @Synchronized
    fun bindAudioSession(sessionId: Int) {
        releaseEffects()
        activeSessionId = sessionId

        try {
            equalizer = Equalizer(1000, sessionId).apply {
                enabled = _state.value.isEnabled && !_state.value.isBypassActive
            }
        } catch (_: Exception) {}

        try {
            bassBoost = BassBoost(1000, sessionId).apply {
                enabled = _state.value.bassBoostEnabled && !_state.value.isBypassActive
                if (strengthSupported) {
                    setStrength(_state.value.bassBoostStrength.toShort())
                }
            }
        } catch (_: Exception) {}

        try {
            virtualizer = Virtualizer(1000, sessionId).apply {
                enabled = _state.value.virtualizerEnabled && !_state.value.isBypassActive
                if (strengthSupported) {
                    setStrength(_state.value.virtualizerStrength.toShort())
                }
            }
        } catch (_: Exception) {}

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                // Initialize DynamicsProcessing on API 28+ with 10 Pre-EQ bands
                val builder = DynamicsProcessing.Config.Builder(
                    DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION,
                    1, // channel count
                    true, // pre-EQ
                    standardFrequencies.size, // 10 bands
                    true, // MBC
                    4,    // MBC bands
                    false, // post-EQ
                    0,
                    true  // Limiter
                )
                dynamicsProcessing = DynamicsProcessing(1000, sessionId, builder.build()).apply {
                    enabled = _state.value.isEnabled && !_state.value.isBypassActive
                }
            } catch (_: Exception) {}
        }

        try {
            loudnessEnhancer = LoudnessEnhancer(sessionId).apply {
                enabled = _state.value.isEnabled && !_state.value.isBypassActive
            }
        } catch (_: Exception) {}

        applyCurrentStateToHardware()
    }

    fun applyDriver(air: AcousticIntermediateRepresentation) {
        val cap = _capability.value
        val mappedBands = translator.mapEqBandsToDevice(air.graphicEq, cap.bandFrequenciesHz)

        _state.update { curr ->
            curr.copy(
                activeDriver = air,
                isEnabled = true,
                isBypassActive = false,
                preampDb = air.preampDb,
                eqBandGains = mappedBands,
                bassBoostEnabled = air.bassBoost?.enabled ?: false,
                bassBoostStrength = air.bassBoost?.strength ?: 0,
                virtualizerEnabled = air.stereoSpatial?.enabled ?: false,
                virtualizerStrength = air.stereoSpatial?.strength ?: 0,
                dynamicsEnabled = air.compressor?.enabled ?: false,
                compressorThresholdDb = air.compressor?.thresholdDb ?: -12f,
                compressorRatio = air.compressor?.ratio ?: 2f,
                limiterCeilingDb = air.limiter?.ceilingDb ?: -0.5f,
                volumeCompensationDb = if (air.masterGainDb != 0f) air.masterGainDb else 0f
            )
        }
        applyCurrentStateToHardware()
    }

    fun setMasterEnabled(enabled: Boolean) {
        _state.update { it.copy(isEnabled = enabled) }
        applyCurrentStateToHardware()
    }

    fun setBypass(bypass: Boolean) {
        _state.update { it.copy(isBypassActive = bypass) }
        applyCurrentStateToHardware()
    }

    private fun getVocalBoostForBand(
        bandIndex: Int,
        enabled: Boolean,
        strength: Int,
        targetMode: VocalTargetMode = _state.value.vocalTargetMode,
        focus: VocalFocus = _state.value.vocalIsolationFocus
    ): Float {
        if (!enabled || strength <= 0) return 0f
        val factor = (strength / 100f).coerceIn(0f, 1f)

        return when (targetMode) {
            VocalTargetMode.BALANCED -> {
                // Focus squarely on 2000 Hz (F2/F3 linguistic formant sweet spot)
                when (bandIndex) {
                    3 -> if (focus == VocalFocus.SURGICAL) -factor * 1.0f else -factor * 0.5f // 250 Hz instrument de-masking
                    4 -> if (focus == VocalFocus.SURGICAL) -factor * 1.6f else -factor * 0.8f // 500 Hz instrument mud de-clutter
                    5 -> if (focus == VocalFocus.SURGICAL) factor * 1.8f else factor * 2.8f   // 1000 Hz vocal body
                    6 -> factor * 6.5f                                                        // 2000 Hz primary vocal formant peak
                    7 -> if (focus == VocalFocus.SURGICAL) 0.0f else factor * 1.5f           // 4000 Hz (zero in surgical to prevent cymbal/guitar sizzle)
                    else -> 0f
                }
            }
            VocalTargetMode.CRISP_SPEECH -> {
                // Shifted toward speech consonants, female range & podcasts (2.5 kHz - 4 kHz)
                when (bandIndex) {
                    3 -> -factor * 1.2f // 250 Hz
                    4 -> -factor * 2.0f // 500 Hz
                    5 -> factor * 0.8f  // 1000 Hz
                    6 -> factor * 4.8f  // 2000 Hz
                    7 -> factor * 5.8f  // 4000 Hz consonant articulation & speech shine
                    else -> 0f
                }
            }
            VocalTargetMode.WARM_MALE -> {
                // Shifted toward lower vocal chest resonance and baritone fundamentals (1 kHz - 2 kHz)
                when (bandIndex) {
                    3 -> -factor * 0.8f // 250 Hz
                    4 -> 0.0f           // Retain lower chest resonance
                    5 -> factor * 5.5f  // 1000 Hz primary chest body
                    6 -> factor * 3.8f  // 2000 Hz
                    7 -> 0.0f           // No boost to keep warmth
                    else -> 0f
                }
            }
        }
    }

    fun setEqBandGain(bandIndex: Int, gainDb: Float) {
        _state.update { curr ->
            val bands = curr.eqBandGains.toMutableList()
            if (bandIndex in bands.indices) {
                bands[bandIndex] = gainDb
            } else {
                while (bands.size <= bandIndex) bands.add(0f)
                bands[bandIndex] = gainDb
            }
            curr.copy(eqBandGains = bands)
        }
        val vocalBoost = getVocalBoostForBand(
            bandIndex,
            _state.value.vocalBoosterEnabled,
            _state.value.vocalBoosterStrength,
            _state.value.vocalTargetMode,
            _state.value.vocalIsolationFocus
        )
        val effectiveGain = gainDb + vocalBoost

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && dynamicsProcessing != null) {
            try {
                if (bandIndex in standardFrequencies.indices) {
                    dynamicsProcessing?.setPreEqBandAllChannelsTo(
                        bandIndex,
                        DynamicsProcessing.EqBand(true, standardFrequencies[bandIndex].toFloat(), effectiveGain)
                    )
                }
            } catch (_: Exception) {}
        } else {
            try {
                equalizer?.let { eq ->
                    if (bandIndex < eq.numberOfBands) {
                        val level = (effectiveGain * 100).toInt().coerceIn(eq.bandLevelRange[0].toInt(), eq.bandLevelRange[1].toInt())
                        eq.setBandLevel(bandIndex.toShort(), level.toShort())
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun setBassBoost(enabled: Boolean, strength: Int) {
        _state.update { it.copy(bassBoostEnabled = enabled, bassBoostStrength = strength) }
        try {
            bassBoost?.apply {
                this.enabled = enabled && !_state.value.isBypassActive
                if (strengthSupported) setStrength(strength.toShort())
            }
        } catch (_: Exception) {}
    }

    fun setVocalBooster(
        enabled: Boolean,
        strength: Int,
        targetMode: VocalTargetMode = _state.value.vocalTargetMode,
        focus: VocalFocus = _state.value.vocalIsolationFocus
    ) {
        _state.update {
            it.copy(
                vocalBoosterEnabled = enabled,
                vocalBoosterStrength = strength,
                vocalTargetMode = targetMode,
                vocalIsolationFocus = focus
            )
        }
        applyCurrentStateToHardware()
    }

    fun setVirtualizer(enabled: Boolean, strength: Int) {
        _state.update { it.copy(virtualizerEnabled = enabled, virtualizerStrength = strength) }
        try {
            virtualizer?.apply {
                this.enabled = enabled && !_state.value.isBypassActive
                if (strengthSupported) setStrength(strength.toShort())
            }
        } catch (_: Exception) {}
    }

    fun setDynamics(enabled: Boolean, thresholdDb: Float, ratio: Float, ceilingDb: Float) {
        _state.update {
            it.copy(
                dynamicsEnabled = enabled,
                compressorThresholdDb = thresholdDb,
                compressorRatio = ratio,
                limiterCeilingDb = ceilingDb
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                dynamicsProcessing?.enabled = _state.value.isEnabled && !_state.value.isBypassActive
            } catch (_: Exception) {}
        }
    }

    fun setOutputDevice(device: OutputDevice) {
        _state.update { it.copy(currentOutput = device) }
    }

    private fun applyCurrentStateToHardware() {
        val st = _state.value
        val active = st.isEnabled && !st.isBypassActive

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && dynamicsProcessing != null) {
            try {
                dynamicsProcessing?.apply {
                    enabled = active
                    st.eqBandGains.forEachIndexed { idx, gainDb ->
                        if (idx < standardFrequencies.size) {
                            val vocalBoost = getVocalBoostForBand(
                                idx,
                                st.vocalBoosterEnabled,
                                st.vocalBoosterStrength,
                                st.vocalTargetMode,
                                st.vocalIsolationFocus
                            )
                            val effectiveGain = gainDb + vocalBoost
                            setPreEqBandAllChannelsTo(
                                idx,
                                DynamicsProcessing.EqBand(true, standardFrequencies[idx].toFloat(), effectiveGain)
                            )
                        }
                    }
                }
                equalizer?.enabled = false // Avoid double EQ filtering
            } catch (_: Exception) {}
        } else {
            try {
                equalizer?.apply {
                    enabled = active
                    st.eqBandGains.forEachIndexed { idx, gainDb ->
                        if (idx < numberOfBands) {
                            val vocalBoost = getVocalBoostForBand(
                                idx,
                                st.vocalBoosterEnabled,
                                st.vocalBoosterStrength,
                                st.vocalTargetMode,
                                st.vocalIsolationFocus
                            )
                            val effectiveGain = gainDb + vocalBoost
                            val level = (effectiveGain * 100).toInt().coerceIn(bandLevelRange[0].toInt(), bandLevelRange[1].toInt())
                            setBandLevel(idx.toShort(), level.toShort())
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        try {
            bassBoost?.apply {
                enabled = active && st.bassBoostEnabled
                if (strengthSupported) setStrength(st.bassBoostStrength.toShort())
            }
        } catch (_: Exception) {}

        try {
            virtualizer?.apply {
                enabled = active && st.virtualizerEnabled
                if (strengthSupported) setStrength(st.virtualizerStrength.toShort())
            }
        } catch (_: Exception) {}

        try {
            loudnessEnhancer?.apply {
                enabled = active && st.volumeCompensationDb > 0f
                if (enabled) {
                    setTargetGain((st.volumeCompensationDb * 100).toInt())
                }
            }
        } catch (_: Exception) {}
    }

    private fun releaseEffects() {
        try { equalizer?.release() } catch (_: Exception) {}
        try { bassBoost?.release() } catch (_: Exception) {}
        try { virtualizer?.release() } catch (_: Exception) {}
        try { loudnessEnhancer?.release() } catch (_: Exception) {}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try { dynamicsProcessing?.release() } catch (_: Exception) {}
        }
        equalizer = null
        bassBoost = null
        virtualizer = null
        loudnessEnhancer = null
        dynamicsProcessing = null
    }

    fun shutdown() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && audioDeviceCallback != null) {
                audioManager?.unregisterAudioDeviceCallback(audioDeviceCallback)
                audioDeviceCallback = null
            }
            headsetReceiver?.let {
                context.unregisterReceiver(it)
                headsetReceiver = null
            }
        } catch (_: Exception) {}
        releaseEffects()
    }
}
