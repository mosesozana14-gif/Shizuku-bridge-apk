package com.example.shizuku

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

data class ShizukuState(
    val isAvailable: Boolean = false,
    val isRunning: Boolean = false,
    val hasPermission: Boolean = false,
    val version: Int = 0,
    val uid: Int = -1,
    val statusMessage: String = "Checking Shizuku status..."
)

data class CommandResult(
    val isSuccess: Boolean,
    val exitCode: Int,
    val output: String,
    val error: String = ""
)

class ShizukuManager(private val context: Context) {

    private val _state = MutableStateFlow(ShizukuState())
    val state: StateFlow<ShizukuState> = _state.asStateFlow()

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        checkStatus()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        _state.value = _state.value.copy(
            isRunning = false,
            hasPermission = false,
            statusMessage = "Shizuku service disconnected"
        )
    }

    private val permissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == SHIZUKU_PERMISSION_REQUEST_CODE) {
            val granted = grantResult == PackageManager.PERMISSION_GRANTED
            val currentUid = try { Shizuku.getUid() } catch (_: Throwable) { -1 }
            _state.value = _state.value.copy(
                hasPermission = granted,
                uid = currentUid,
                statusMessage = if (granted) "Shizuku authorization granted (UID: $currentUid)" else "Shizuku permission denied by user"
            )
        }
    }

    fun initialize() {
        try {
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(permissionResultListener)
            checkStatus()
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isAvailable = false,
                isRunning = false,
                hasPermission = false,
                statusMessage = "Shizuku init error: ${e.message}"
            )
        }
    }

    fun destroy() {
        try {
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
            Shizuku.removeRequestPermissionResultListener(permissionResultListener)
        } catch (_: Exception) {}
    }

    fun checkStatus() {
        val ping = try {
            Shizuku.pingBinder()
        } catch (_: Throwable) {
            false
        }

        if (!ping) {
            _state.value = ShizukuState(
                isAvailable = true,
                isRunning = false,
                hasPermission = false,
                version = 0,
                uid = -1,
                statusMessage = "Shizuku service is not running. Please start Shizuku."
            )
            return
        }

        val version = try {
            Shizuku.getVersion()
        } catch (_: Throwable) {
            0
        }

        val uid = try {
            Shizuku.getUid()
        } catch (_: Throwable) {
            -1
        }

        val hasPermission = try {
            if (version < 11) {
                false
            } else {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            }
        } catch (_: Throwable) {
            false
        }

        _state.value = ShizukuState(
            isAvailable = true,
            isRunning = true,
            hasPermission = hasPermission,
            version = version,
            uid = uid,
            statusMessage = if (hasPermission) "Connected & authorized (UID: $uid)" else "Connected. Permission required."
        )
    }

    fun requestPermission() {
        try {
            val isPing = try { Shizuku.pingBinder() } catch (_: Throwable) { false }
            if (!isPing) {
                _state.value = _state.value.copy(statusMessage = "Shizuku service is not running. Please launch the Shizuku app first.")
                return
            }

            val version = try { Shizuku.getVersion() } catch (_: Throwable) { 0 }
            if (version < 11) {
                _state.value = _state.value.copy(statusMessage = "Shizuku API version is too old (< v11)")
                return
            }

            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                _state.value = _state.value.copy(hasPermission = true, statusMessage = "Permission already granted")
                return
            }

            if (Shizuku.shouldShowRequestPermissionRationale()) {
                _state.value = _state.value.copy(statusMessage = "Please approve Shizuku permission in the system dialog")
            }

            Shizuku.requestPermission(SHIZUKU_PERMISSION_REQUEST_CODE)
        } catch (e: Exception) {
            _state.value = _state.value.copy(statusMessage = "Failed to request permission: ${e.message}")
        }
    }

    suspend fun executePrivilegedCommand(vararg commandArgs: String): CommandResult = withContext(Dispatchers.IO) {
        if (!_state.value.isRunning || !_state.value.hasPermission) {
            return@withContext CommandResult(
                isSuccess = false,
                exitCode = -1,
                output = "",
                error = "Shizuku service is not running or permission is not granted"
            )
        }

        try {
            val fullCommand: Array<String> = if (commandArgs.size == 1 && (commandArgs[0].contains(" ") || commandArgs[0].contains("|") || commandArgs[0].contains("&") || commandArgs[0].contains(";"))) {
                arrayOf("sh", "-c", commandArgs[0])
            } else {
                commandArgs.toList().toTypedArray()
            }

            val process: Process = try {
                val newProcessMethod = Shizuku::class.java.getDeclaredMethod(
                    "newProcess",
                    Array<String>::class.java,
                    Array<String>::class.java,
                    String::class.java
                )
                newProcessMethod.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                newProcessMethod.invoke(null, fullCommand, null, null) as Process
            } catch (_: Throwable) {
                Runtime.getRuntime().exec(fullCommand)
            }

            val output = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }
            val error = BufferedReader(InputStreamReader(process.errorStream)).use { it.readText() }
            val exitCode = process.waitFor()

            CommandResult(
                isSuccess = exitCode == 0,
                exitCode = exitCode,
                output = output.trim(),
                error = error.trim()
            )
        } catch (e: Exception) {
            CommandResult(
                isSuccess = false,
                exitCode = -1,
                output = "",
                error = "Execution exception: ${e.localizedMessage ?: e.message}"
            )
        }
    }

    suspend fun grantSystemSettingsPermissions(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val packageName = context.packageName
        val log = StringBuilder()

        // 1. Grant WRITE_SECURE_SETTINGS via pm grant
        val resSecure = executePrivilegedCommand("pm", "grant", packageName, "android.permission.WRITE_SECURE_SETTINGS")
        log.append("WRITE_SECURE_SETTINGS (pm grant): ")
            .append(if (resSecure.isSuccess) "Success (Granted)" else "Exit ${resSecure.exitCode}: ${resSecure.error.ifBlank { resSecure.output.ifBlank { "OK" } }}")
            .append("\n")

        // 2. Grant WRITE_SETTINGS via pm grant
        val resSettings = executePrivilegedCommand("pm", "grant", packageName, "android.permission.WRITE_SETTINGS")
        log.append("WRITE_SETTINGS (pm grant): ")
            .append(if (resSettings.isSuccess) "Success (Granted)" else "Exit ${resSettings.exitCode}: ${resSettings.error.ifBlank { resSettings.output.ifBlank { "OK" } }}")
            .append("\n")

        // 3. Grant WRITE_SETTINGS via appops set (required for Android 6.0+)
        val resAppOps = executePrivilegedCommand("cmd", "appops", "set", packageName, "WRITE_SETTINGS", "allow")
        val resAppOpsFallback = if (!resAppOps.isSuccess) {
            executePrivilegedCommand("appops", "set", packageName, "WRITE_SETTINGS", "allow")
        } else {
            resAppOps
        }
        log.append("AppOps WRITE_SETTINGS: ")
            .append(if (resAppOpsFallback.isSuccess) "Allowed" else "${resAppOpsFallback.error.ifBlank { resAppOpsFallback.output }}")

        val overallSuccess = resSecure.isSuccess || resSettings.isSuccess || resAppOpsFallback.isSuccess
        Pair(overallSuccess, log.toString())
    }

    fun launchShizukuApp(): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")
            if (intent != null) {
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    suspend fun runDiagnosticTest(): CommandResult = withContext(Dispatchers.IO) {
        executePrivilegedCommand("id; getprop ro.build.version.release; getprop ro.product.model")
    }

    companion object {
        const val SHIZUKU_PERMISSION_REQUEST_CODE = 8821
    }
}
