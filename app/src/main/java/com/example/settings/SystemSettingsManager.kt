package com.example.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.example.shizuku.ShizukuManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

data class SystemSettingsStatus(
    val canWriteSystem: Boolean = false,
    val hasSecureSettingsPermission: Boolean = false,
    val windowAnimationScale: Float = 1.0f,
    val transitionAnimationScale: Float = 1.0f,
    val animatorDurationScale: Float = 1.0f,
    val screenTimeoutMs: Int = 30000,
    val peakRefreshRate: Float = 60.0f,
    val hapticEnabled: Boolean = true,
    val soundEffectsEnabled: Boolean = true,
    val statusMessage: String = ""
)

enum class SettingNamespace {
    SYSTEM, SECURE, GLOBAL
}

class SystemSettingsManager(
    private val context: Context,
    private val shizukuManager: ShizukuManager
) {
    private val _status = MutableStateFlow(SystemSettingsStatus())
    val status: StateFlow<SystemSettingsStatus> = _status.asStateFlow()

    fun refreshStatus() {
        val canWrite = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(context)
        } else {
            true
        }

        val hasSecure = context.checkCallingOrSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED

        val windowScale = try {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.WINDOW_ANIMATION_SCALE, 1.0f)
        } catch (_: Exception) { 1.0f }

        val transitionScale = try {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.TRANSITION_ANIMATION_SCALE, 1.0f)
        } catch (_: Exception) { 1.0f }

        val animatorScale = try {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f)
        } catch (_: Exception) { 1.0f }

        val timeout = try {
            Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, 30000)
        } catch (_: Exception) { 30000 }

        val haptic = try {
            Settings.System.getInt(context.contentResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) == 1
        } catch (_: Exception) { true }

        val sound = try {
            Settings.System.getInt(context.contentResolver, Settings.System.SOUND_EFFECTS_ENABLED, 1) == 1
        } catch (_: Exception) { true }

        val refreshRate = try {
            Settings.System.getFloat(context.contentResolver, "peak_refresh_rate", 60.0f)
        } catch (_: Exception) { 60.0f }

        _status.value = SystemSettingsStatus(
            canWriteSystem = canWrite,
            hasSecureSettingsPermission = hasSecure,
            windowAnimationScale = windowScale,
            transitionAnimationScale = transitionScale,
            animatorDurationScale = animatorScale,
            screenTimeoutMs = timeout,
            peakRefreshRate = refreshRate,
            hapticEnabled = haptic,
            soundEffectsEnabled = sound,
            statusMessage = when {
                canWrite && hasSecure -> "Active: Full System & Secure Settings access enabled"
                canWrite -> "Active: Standard System Settings write access enabled"
                hasSecure -> "Active: Secure Settings access enabled"
                else -> "Permission required: Authorize via Shizuku or Android Settings"
            }
        )
    }

    fun getRequestWriteSettingsIntent(): Intent {
        return Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    suspend fun setAnimationScales(scale: Float): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val scaleStr = if (scale == 0f) "0" else scale.toString()
        var success = false

        // 1. Try real privileged Shizuku command
        if (shizukuManager.state.value.isRunning && shizukuManager.state.value.hasPermission) {
            val resWin = shizukuManager.executePrivilegedCommand("settings", "put", "global", "window_animation_scale", scaleStr)
            val resTrans = shizukuManager.executePrivilegedCommand("settings", "put", "global", "transition_animation_scale", scaleStr)
            val resAnim = shizukuManager.executePrivilegedCommand("settings", "put", "global", "animator_duration_scale", scaleStr)
            success = resWin.isSuccess && resTrans.isSuccess && resAnim.isSuccess
        }

        // 2. Fallback to ContentResolver if WRITE_SECURE_SETTINGS is granted
        if (!success && context.checkCallingOrSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED) {
            try {
                Settings.Global.putFloat(context.contentResolver, Settings.Global.WINDOW_ANIMATION_SCALE, scale)
                Settings.Global.putFloat(context.contentResolver, Settings.Global.TRANSITION_ANIMATION_SCALE, scale)
                Settings.Global.putFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, scale)
                success = true
            } catch (e: Exception) {
                return@withContext Pair(false, "Failed to apply via SDK: ${e.message}")
            }
        }

        refreshStatus()
        Pair(success, if (success) "Animation scales applied: ${scale}x" else "Failed to apply animation scales. Shizuku authorization required.")
    }

    suspend fun setScreenTimeout(milliseconds: Int): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        var success = false

        // Try via Shizuku first
        if (shizukuManager.state.value.isRunning && shizukuManager.state.value.hasPermission) {
            val res = shizukuManager.executePrivilegedCommand("settings", "put", "system", "screen_off_timeout", milliseconds.toString())
            if (res.isSuccess) success = true
        }

        // Fallback to ContentResolver if canWriteSystem is true
        if (!success && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.System.canWrite(context)) {
            try {
                Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, milliseconds)
                success = true
            } catch (_: Exception) {}
        }

        refreshStatus()
        Pair(success, if (success) "Screen timeout updated to ${milliseconds / 1000}s" else "Failed to update timeout. Grant WRITE_SETTINGS permission.")
    }

    suspend fun setHapticFeedback(enabled: Boolean): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val value = if (enabled) "1" else "0"
        var success = false

        if (shizukuManager.state.value.isRunning && shizukuManager.state.value.hasPermission) {
            val res = shizukuManager.executePrivilegedCommand("settings", "put", "system", "haptic_feedback_enabled", value)
            if (res.isSuccess) success = true
        }

        if (!success && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.System.canWrite(context)) {
            try {
                Settings.System.putInt(context.contentResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, if (enabled) 1 else 0)
                success = true
            } catch (_: Exception) {}
        }

        refreshStatus()
        Pair(success, if (success) "Haptic feedback ${if (enabled) "enabled" else "disabled"}" else "Failed to update haptics.")
    }

    suspend fun setPeakRefreshRate(rateHz: Float): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val rateStr = rateHz.toInt().toString()
        var success = false

        if (shizukuManager.state.value.isRunning && shizukuManager.state.value.hasPermission) {
            val resPeak = shizukuManager.executePrivilegedCommand("settings", "put", "system", "peak_refresh_rate", rateStr)
            val resMin = shizukuManager.executePrivilegedCommand("settings", "put", "system", "min_refresh_rate", rateStr)
            val resGlobal = shizukuManager.executePrivilegedCommand("settings", "put", "global", "peak_refresh_rate", rateStr)
            success = resPeak.isSuccess || resMin.isSuccess || resGlobal.isSuccess
        }

        if (!success && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.System.canWrite(context)) {
            try {
                Settings.System.putFloat(context.contentResolver, "peak_refresh_rate", rateHz)
                success = true
            } catch (_: Exception) {}
        }

        refreshStatus()
        Pair(success, if (success) "Target refresh rate set to ${rateHz.toInt()}Hz" else "Failed to set refresh rate.")
    }

    suspend fun writeCustomSetting(
        namespace: SettingNamespace,
        key: String,
        value: String
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (key.isBlank()) return@withContext Pair(false, "Setting key cannot be blank")

        val namespaceStr = when (namespace) {
            SettingNamespace.SYSTEM -> "system"
            SettingNamespace.SECURE -> "secure"
            SettingNamespace.GLOBAL -> "global"
        }

        if (shizukuManager.state.value.isRunning && shizukuManager.state.value.hasPermission) {
            val res = shizukuManager.executePrivilegedCommand("settings", "put", namespaceStr, key.trim(), value.trim())
            if (res.isSuccess) {
                return@withContext Pair(true, "Successfully set $namespaceStr.${key.trim()} = ${value.trim()} via Shizuku")
            } else {
                return@withContext Pair(false, "Command error: ${res.error.ifEmpty { res.output }}")
            }
        }

        // Native API attempt
        try {
            when (namespace) {
                SettingNamespace.SYSTEM -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(context)) {
                        return@withContext Pair(false, "Cannot write system settings: WRITE_SETTINGS permission missing")
                    }
                    Settings.System.putString(context.contentResolver, key.trim(), value.trim())
                    Pair(true, "Saved $namespaceStr.${key.trim()} via ContentResolver")
                }
                SettingNamespace.SECURE -> {
                    if (context.checkCallingOrSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
                        return@withContext Pair(false, "WRITE_SECURE_SETTINGS permission missing")
                    }
                    Settings.Secure.putString(context.contentResolver, key.trim(), value.trim())
                    Pair(true, "Saved $namespaceStr.${key.trim()} via ContentResolver")
                }
                SettingNamespace.GLOBAL -> {
                    if (context.checkCallingOrSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
                        return@withContext Pair(false, "WRITE_SECURE_SETTINGS permission missing for Global settings")
                    }
                    Settings.Global.putString(context.contentResolver, key.trim(), value.trim())
                    Pair(true, "Saved $namespaceStr.${key.trim()} via ContentResolver")
                }
            }
        } catch (e: Exception) {
            Pair(false, "Write failed: ${e.message}")
        }
    }

    suspend fun readCustomSetting(
        namespace: SettingNamespace,
        key: String
    ): String = withContext(Dispatchers.IO) {
        if (key.isBlank()) return@withContext "N/A"

        // Read direct from ContentResolver first
        try {
            val directVal = when (namespace) {
                SettingNamespace.SYSTEM -> Settings.System.getString(context.contentResolver, key.trim())
                SettingNamespace.SECURE -> Settings.Secure.getString(context.contentResolver, key.trim())
                SettingNamespace.GLOBAL -> Settings.Global.getString(context.contentResolver, key.trim())
            }
            if (!directVal.isNullOrBlank() && directVal != "null") {
                return@withContext directVal
            }
        } catch (_: Exception) {}

        // Fallback to Shizuku command
        if (shizukuManager.state.value.isRunning && shizukuManager.state.value.hasPermission) {
            val namespaceStr = when (namespace) {
                SettingNamespace.SYSTEM -> "system"
                SettingNamespace.SECURE -> "secure"
                SettingNamespace.GLOBAL -> "global"
            }
            val res = shizukuManager.executePrivilegedCommand("settings", "get", namespaceStr, key.trim())
            if (res.isSuccess && res.output.isNotBlank() && res.output != "null") {
                return@withContext res.output
            }
        }

        "null / not set"
    }
}
