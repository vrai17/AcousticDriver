package com.vraidev.acousticdriver.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Splash : Screen("splash", "Splash", Icons.Filled.Audiotrack, Icons.Outlined.Audiotrack)
    object Home : Screen("home", "Home", Icons.Filled.Headphones, Icons.Outlined.Headphones)
    object Drivers : Screen("drivers", "Drivers", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic)
    object Equalizer : Screen("equalizer", "EQ", Icons.Filled.Equalizer, Icons.Outlined.Equalizer)
    object Effects : Screen("effects", "Rack", Icons.Filled.Tune, Icons.Outlined.Tune)
    object Device : Screen("device", "Device", Icons.Filled.Smartphone, Icons.Outlined.Smartphone)
    object About : Screen("about", "About", Icons.Filled.Info, Icons.Outlined.Info)

    companion object {
        val bottomNavScreens = listOf(Home, Drivers, Equalizer, Effects, Device)
    }
}
