package com.vraidev.acousticdriver.core.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat

class BackgroundPersistenceHelper(private val context: Context) {

    private val prefs = context.getSharedPreferences("persistence_prefs", Context.MODE_PRIVATE)

    fun isNotificationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun isSystemBatteryOptimizationIgnored(): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return true
        return try {
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } catch (_: Exception) {
            false
        }
    }

    fun isBatteryOptimizationIgnored(): Boolean {
        if (prefs.getBoolean("battery_opt_manually_acknowledged", false)) {
            return true
        }
        return isSystemBatteryOptimizationIgnored()
    }

    fun setBatteryOptimizationManuallyAcknowledged(acknowledged: Boolean) {
        prefs.edit().putBoolean("battery_opt_manually_acknowledged", acknowledged).apply()
    }

    fun isLockInRecentsDone(): Boolean {
        return prefs.getBoolean("lock_in_recents_acknowledged", false)
    }

    fun setLockInRecentsDone(done: Boolean) {
        prefs.edit().putBoolean("lock_in_recents_acknowledged", done).apply()
    }

    fun hasSeenInitialPopup(): Boolean {
        return prefs.getBoolean("has_seen_initial_persistence_popup", false)
    }

    fun setHasSeenInitialPopup(seen: Boolean) {
        prefs.edit().putBoolean("has_seen_initial_persistence_popup", seen).apply()
    }

    fun getPendingCount(): Int {
        var count = 0
        if (!isNotificationPermissionGranted()) count++
        if (!isBatteryOptimizationIgnored()) count++
        if (!isLockInRecentsDone()) count++
        return count
    }

    @SuppressLint("BatteryLife")
    fun createRequestBatteryOptimizationIntent(): Intent {
        return Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    }

    fun createAppDetailsSettingsIntent(): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    }
}
