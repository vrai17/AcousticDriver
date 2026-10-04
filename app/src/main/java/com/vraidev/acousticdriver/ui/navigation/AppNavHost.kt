package com.vraidev.acousticdriver.ui.navigation

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.vraidev.acousticdriver.audio.AudioEngine
import com.vraidev.acousticdriver.core.service.BackgroundPersistenceHelper
import com.vraidev.acousticdriver.core.translator.AcousticTranslator
import com.vraidev.acousticdriver.data.repository.DriverRepository
import com.vraidev.acousticdriver.ui.components.BackgroundProtectionDialog
import com.vraidev.acousticdriver.ui.components.TopBar
import com.vraidev.acousticdriver.ui.screens.about.AboutScreen
import com.vraidev.acousticdriver.ui.screens.device.DeviceScreen
import com.vraidev.acousticdriver.ui.screens.drivers.DriverBrowserScreen
import com.vraidev.acousticdriver.ui.screens.effects.EffectsRackScreen
import com.vraidev.acousticdriver.ui.screens.equalizer.EqualizerScreen
import com.vraidev.acousticdriver.ui.screens.home.HomeScreen
import com.vraidev.acousticdriver.ui.screens.splash.SplashScreen
import com.vraidev.acousticdriver.ui.theme.*

@Composable
fun AppNavHost(
    audioEngine: AudioEngine,
    repository: DriverRepository,
    translator: AcousticTranslator
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val persistenceHelper = remember { BackgroundPersistenceHelper(context) }

    var isNotificationGranted by remember { mutableStateOf(persistenceHelper.isNotificationPermissionGranted()) }
    var isBatteryIgnored by remember { mutableStateOf(persistenceHelper.isBatteryOptimizationIgnored()) }
    var isLockInRecentsDone by remember { mutableStateOf(persistenceHelper.isLockInRecentsDone()) }

    var showPersistenceDialog by remember { mutableStateOf(false) }

    val refreshPersistenceStatus = {
        isNotificationGranted = persistenceHelper.isNotificationPermissionGranted()
        isBatteryIgnored = persistenceHelper.isBatteryOptimizationIgnored()
        isLockInRecentsDone = persistenceHelper.isLockInRecentsDone()
    }

    // Automatically re-query permissions and optimization flags whenever returning from Settings or background
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshPersistenceStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val pendingPersistenceCount = (if (!isNotificationGranted) 1 else 0) +
            (if (!isBatteryIgnored) 1 else 0) +
            (if (!isLockInRecentsDone) 1 else 0)

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        isNotificationGranted = granted
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val audioState by audioEngine.state.collectAsState()

    val isSplash = currentRoute == Screen.Splash.route

    // Check on fresh open when leaving splash screen
    LaunchedEffect(currentRoute) {
        if (currentRoute == Screen.Home.route) {
            refreshPersistenceStatus()

            if (!persistenceHelper.hasSeenInitialPopup() && persistenceHelper.getPendingCount() > 0) {
                showPersistenceDialog = true
                persistenceHelper.setHasSeenInitialPopup(true)
            }
        }
    }

    if (showPersistenceDialog) {
        BackgroundProtectionDialog(
            isNotificationGranted = isNotificationGranted,
            onRequestNotification = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    isNotificationGranted = true
                }
            },
            isBatteryIgnored = isBatteryIgnored,
            onRequestBatteryOptimization = {
                try {
                    context.startActivity(persistenceHelper.createRequestBatteryOptimizationIntent())
                } catch (_: Exception) {
                    try {
                        context.startActivity(persistenceHelper.createAppDetailsSettingsIntent())
                    } catch (_: Exception) {}
                }
            },
            onToggleBatteryOptimizationDone = { done ->
                persistenceHelper.setBatteryOptimizationManuallyAcknowledged(done)
                refreshPersistenceStatus()
            },
            onRecheckStatus = {
                refreshPersistenceStatus()
            },
            isLockInRecentsDone = isLockInRecentsDone,
            onToggleLockInRecents = { done ->
                isLockInRecentsDone = done
                persistenceHelper.setLockInRecentsDone(done)
            },
            onOpenAppSettings = {
                try {
                    context.startActivity(persistenceHelper.createAppDetailsSettingsIntent())
                } catch (_: Exception) {}
            },
            onDismiss = {
                showPersistenceDialog = false
            }
        )
    }

    Scaffold(
        containerColor = BgDark,
        topBar = {
            if (!isSplash) {
                TopBar(
                    currentOutput = audioState.currentOutput,
                    onOutputSelected = { audioEngine.setOutputDevice(it) },
                    onAboutClick = { navController.navigate(Screen.About.route) },
                    isEnabled = audioState.isEnabled,
                    onToggleEnabled = { audioEngine.setMasterEnabled(!audioState.isEnabled) },
                    pendingPersistenceCount = pendingPersistenceCount,
                    onOpenPersistenceSetup = { showPersistenceDialog = true }
                )
            }
        },
        bottomBar = {
            if (!isSplash) {
                NavigationBar(
                    containerColor = SurfaceDark,
                    tonalElevation = 8.dp
                ) {
                    Screen.bottomNavScreens.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 11.sp,
                                    color = if (selected) CyberCyan else TextSecondary
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BgDark,
                                indicatorColor = CyberCyan,
                                unselectedIconColor = TextSecondary
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    audioEngine = audioEngine,
                    translator = translator,
                    onNavigateToDrivers = { navController.navigate(Screen.Drivers.route) },
                    onNavigateToEq = { navController.navigate(Screen.Equalizer.route) },
                    onNavigateToEffects = { navController.navigate(Screen.Effects.route) }
                )
            }

            composable(Screen.Drivers.route) {
                DriverBrowserScreen(
                    audioEngine = audioEngine,
                    repository = repository,
                    translator = translator
                )
            }

            composable(Screen.Equalizer.route) {
                EqualizerScreen(
                    audioEngine = audioEngine,
                    repository = repository
                )
            }

            composable(Screen.Effects.route) {
                EffectsRackScreen(audioEngine = audioEngine)
            }

            composable(Screen.Device.route) {
                DeviceScreen(audioEngine = audioEngine)
            }

            composable(Screen.About.route) {
                AboutScreen(
                    onNavigateToDevice = { navController.navigate(Screen.Device.route) }
                )
            }
        }
    }
}
