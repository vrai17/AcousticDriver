package com.vraidev.acousticdriver

import android.app.Application
import com.vraidev.acousticdriver.audio.AudioEngine
import com.vraidev.acousticdriver.audio.DeviceCapabilityScanner
import com.vraidev.acousticdriver.audio.service.AcousticAudioService
import com.vraidev.acousticdriver.core.translator.AcousticTranslator
import com.vraidev.acousticdriver.data.bundled.BundledDrivers
import com.vraidev.acousticdriver.data.repository.DriverRepository

class AcousticDriverApp : Application() {

    lateinit var capabilityScanner: DeviceCapabilityScanner
        private set

    lateinit var translator: AcousticTranslator
        private set

    lateinit var audioEngine: AudioEngine
        private set

    lateinit var driverRepository: DriverRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        capabilityScanner = DeviceCapabilityScanner(this)
        translator = AcousticTranslator()
        audioEngine = AudioEngine(this, capabilityScanner, translator)
        driverRepository = DriverRepository(this)

        // Initialize audio engine
        audioEngine.initialize()

        // Apply default favorite driver (Mega Acoustic)
        val defaultDriver = BundledDrivers.catalog.firstOrNull()
        if (defaultDriver != null) {
            audioEngine.applyDriver(defaultDriver)
        }

        // Start foreground service
        AcousticAudioService.start(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        audioEngine.shutdown()
    }

    companion object {
        lateinit var instance: AcousticDriverApp
            private set
    }
}
