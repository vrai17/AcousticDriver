package com.vraidev.acousticdriver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.vraidev.acousticdriver.ui.navigation.AppNavHost
import com.vraidev.acousticdriver.ui.theme.AcousticDriverTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install Android 12+ Splash Screen support
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val app = application as AcousticDriverApp

        setContent {
            AcousticDriverTheme {
                AppNavHost(
                    audioEngine = app.audioEngine,
                    repository = app.driverRepository,
                    translator = app.translator
                )
            }
        }
    }
}
