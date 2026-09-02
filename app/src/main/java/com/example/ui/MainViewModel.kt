package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitlife.BitLifeManager
import com.example.bitlife.BitLifeSlotInfo
import com.example.bitlife.BitLifeStats
import com.example.overlay.MosesModRepository
import com.example.settings.SettingNamespace
import com.example.settings.SystemSettingsManager
import com.example.shizuku.ShizukuManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConsoleLog(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val isError: Boolean = false,
    val time: String
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val shizukuManager = ShizukuManager(application)
    val settingsManager = SystemSettingsManager(application, shizukuManager)
    val bitLifeManager = BitLifeManager(application, shizukuManager)

    val shizukuState = shizukuManager.state
    val settingsStatus = settingsManager.status
    val bitLifeScanResult = bitLifeManager.scanResult
    val bitLifeStats = bitLifeManager.currentStats
    val bitLifeSelectedSlot = bitLifeManager.selectedSlot
    val selectedGame = bitLifeManager.selectedGame
    val isOverlayActive = MosesModRepository.isOverlayActive
    val isHudExpanded = MosesModRepository.isHudExpanded

    private val _consoleLogs = MutableStateFlow<List<ConsoleLog>>(emptyList())
    val consoleLogs: StateFlow<List<ConsoleLog>> = _consoleLogs.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val timeFormat = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())

    init {
        shizukuManager.initialize()
        settingsManager.refreshStatus()
        MosesModRepository.initialize(bitLifeManager)
        log("Shizuku Manager & Moses Mod Engine initialized. Ready for privileged bridge actions.")
    }

    override fun onCleared() {
        super.onCleared()
        shizukuManager.destroy()
    }

    fun log(text: String, isError: Boolean = false) {
        val entry = ConsoleLog(
            text = text,
            isError = isError,
            time = timeFormat.format(java.util.Date())
        )
        val list = _consoleLogs.value.toMutableList()
        list.add(0, entry)
        if (list.size > 100) list.removeAt(list.size - 1)
        _consoleLogs.value = list
    }

    fun requestShizukuPermission() {
        log("Requesting Shizuku API authorization...")
        shizukuManager.requestPermission()
    }

    fun launchShizuku(): Boolean {
        log("Opening Shizuku application...")
        val opened = shizukuManager.launchShizukuApp()
        if (!opened) {
            log("Could not find Shizuku app. Make sure Shizuku is installed from Google Play or GitHub.", isError = true)
        }
        return opened
    }

    fun testShizukuPrivilege() {
        viewModelScope.launch {
            _isBusy.value = true
            log("Running live privilege verification test (id & system properties)...")
            val res = shizukuManager.runDiagnosticTest()
            if (res.isSuccess) {
                log("✓ Privilege Verification Succeeded!\n${res.output}")
            } else {
                log("Privilege Test Failed (Code ${res.exitCode}): ${res.error.ifBlank { res.output }}", isError = true)
            }
            shizukuManager.checkStatus()
            _isBusy.value = false
        }
    }

    fun refreshAll() {
        shizukuManager.checkStatus()
        settingsManager.refreshStatus()
        if (shizukuState.value.hasPermission) {
            scanBitLife()
        }
        log("Shizuku service & system status refreshed.")
    }

    // --- BitLife Save & Stats Actions ---

    fun autoFindBitLife() {
        viewModelScope.launch {
            _isBusy.value = true
            log("⚡ Initiating Auto-Finder: Crawling storage, Android/data & filesystems for BitLife save data...")
            val result = bitLifeManager.scanBitLifeFiles()
            if (result.availableSlots.isNotEmpty()) {
                val active = result.availableSlots.first()
                log("✓ Auto-Finder Succeeded! Found ${result.availableSlots.size} save file(s). Automatically loaded '${active.slotName}' (${active.characterSummary ?: active.filePath}) into editor.")
            } else if (result.baseDirFound.isNotEmpty()) {
                log("Found BitLife directory (${result.baseDirFound}), but no save character exists yet. Tap 'Auto-Create & Inject God Life' to generate a maxed character instantly!", isError = false)
            } else if (result.error.isNotEmpty()) {
                log(result.error, isError = true)
            } else {
                log("No BitLife save files found automatically. Open BitLife once or tap 'Auto-Create & Inject God Life' to initialize.", isError = false)
            }
            _isBusy.value = false
        }
    }

    fun autoInjectGodLife() {
        viewModelScope.launch {
            _isBusy.value = true
            log("⚡ Auto-Creating & Injecting brand-new God Mode life ($10B cash, 100% stats, all DLCs) into BitLife...")
            val (success, msg) = bitLifeManager.autoInjectNewGodLife()
            log(msg, isError = !success)
            _isBusy.value = false
        }
    }

    fun launchBitLife(): Boolean {
        log("Launching BitLife...")
        val launched = bitLifeManager.launchBitLifeApp()
        if (!launched) {
            log("Could not find BitLife app package to launch. Make sure BitLife is installed.", isError = true)
        }
        return launched
    }

    fun scanBitLife() {
        autoFindBitLife()
    }

    fun selectGame(game: com.example.bitlife.SupportedGame) {
        bitLifeManager.selectGame(game)
        scanBitLife()
    }

    fun selectBitLifeSlot(slot: BitLifeSlotInfo) {
        bitLifeManager.selectSlot(slot)
        viewModelScope.launch {
            _isBusy.value = true
            log("Loading character stats from ${slot.slotName} (${slot.filePath})...")
            val (success, msg) = bitLifeManager.loadStatsFromSlot(slot)
            log(msg, isError = !success)
            _isBusy.value = false
        }
    }

    fun saveBitLifeStats(newStats: BitLifeStats) {
        val slot = bitLifeSelectedSlot.value
        if (slot == null) {
            log("No save slot selected to apply stats to.", isError = true)
            return
        }

        viewModelScope.launch {
            _isBusy.value = true
            log("Patching BitLife save: Happiness=${newStats.happiness}%, Health=${newStats.health}%, Smarts=${newStats.smarts}%, Looks=${newStats.looks}%, Money=\$${newStats.bankBalance}...")
            val (success, msg) = bitLifeManager.saveAndApplyStats(slot, newStats)
            log(msg, isError = !success)
            _isBusy.value = false
        }
    }

    fun unlockBitLifeGodMode() {
        viewModelScope.launch {
            _isBusy.value = true
            log("Injecting MonetizationVars (God Mode, Bitizenship, Boss Mode, All Expansion Packs) via Shizuku...")
            val (success, msg) = bitLifeManager.unlockGodModeAndMonetization()
            log(msg, isError = !success)
            _isBusy.value = false
        }
    }

    fun isZArchiverInstalled(): Boolean = bitLifeManager.isZArchiverInstalled()

    fun launchZArchiver(): Boolean = bitLifeManager.launchZArchiver()

    fun loadPickedFile(uri: Uri) {
        viewModelScope.launch {
            _isBusy.value = true
            log("Opening save file from Storage / ZArchiver: $uri...")
            val (success, msg) = bitLifeManager.loadPickedFile(uri)
            log(msg, isError = !success)
            _isBusy.value = false
        }
    }

    fun exportPatchedSave(targetUri: Uri, newStats: BitLifeStats) {
        viewModelScope.launch {
            _isBusy.value = true
            log("Exporting patched save file to $targetUri...")
            val (success, msg) = bitLifeManager.exportPatchedSave(targetUri, newStats)
            log(msg, isError = !success)
            _isBusy.value = false
        }
    }

    fun exportGodModeMonetization(targetUri: Uri) {
        viewModelScope.launch {
            _isBusy.value = true
            log("Exporting God Mode MonetizationVars to $targetUri...")
            val (success, msg) = bitLifeManager.exportGodModeMonetization(targetUri)
            log(msg, isError = !success)
            _isBusy.value = false
        }
    }

    // --- System Settings Actions ---

    fun grantSystemSettingsPermissionsViaShizuku() {
        viewModelScope.launch {
            _isBusy.value = true
            log("Executing: pm grant for WRITE_SETTINGS & WRITE_SECURE_SETTINGS via Shizuku...")
            val (success, detail) = shizukuManager.grantSystemSettingsPermissions()
            settingsManager.refreshStatus()
            log(detail, isError = !success)
            _isBusy.value = false
        }
    }

    fun setAnimationScale(scale: Float) {
        viewModelScope.launch {
            _isBusy.value = true
            log("Applying animation scale: ${scale}x...")
            val (success, msg) = settingsManager.setAnimationScales(scale)
            log(msg, isError = !success)
            _isBusy.value = false
        }
    }

    fun setScreenTimeout(ms: Int) {
        viewModelScope.launch {
            _isBusy.value = true
            log("Setting screen timeout to ${ms / 1000}s...")
            val (success, msg) = settingsManager.setScreenTimeout(ms)
            log(msg, isError = !success)
            _isBusy.value = false
        }
    }

    fun setHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            _isBusy.value = true
            log("Setting haptic feedback: $enabled...")
            val (success, msg) = settingsManager.setHapticFeedback(enabled)
            log(msg, isError = !success)
            _isBusy.value = false
        }
    }

    fun setPeakRefreshRate(rate: Float) {
        viewModelScope.launch {
            _isBusy.value = true
            log("Setting peak refresh rate: ${rate.toInt()}Hz...")
            val (success, msg) = settingsManager.setPeakRefreshRate(rate)
            log(msg, isError = !success)
            _isBusy.value = false
        }
    }

    fun writeCustomSetting(namespace: SettingNamespace, key: String, value: String) {
        viewModelScope.launch {
            _isBusy.value = true
            log("Writing setting $namespace: $key = $value...")
            val (success, msg) = settingsManager.writeCustomSetting(namespace, key, value)
            log(msg, isError = !success)
            _isBusy.value = false
        }
    }

    fun runCustomShellCommand(command: String) {
        if (command.isBlank()) return
        viewModelScope.launch {
            _isBusy.value = true
            log("$ $command")
            val parts = command.trim().split("\\s+".toRegex())
            val res = shizukuManager.executePrivilegedCommand(*parts.toTypedArray())
            if (res.output.isNotBlank()) log(res.output)
            if (res.error.isNotBlank()) log(res.error, isError = true)
            log("Command completed with exit code ${res.exitCode}")
            _isBusy.value = false
        }
    }

    fun toggleOverlay() {
        val app = getApplication<Application>()
        if (MosesModRepository.isOverlayActive.value) {
            MosesModRepository.stopOverlayService(app)
            log("Moses Mod Floating Overlay stopped.")
        } else {
            if (MosesModRepository.hasOverlayPermission(app)) {
                MosesModRepository.startOverlayService(app)
                log("Moses Mod Floating Overlay started! Floating pill active.")
            } else {
                log("Overlay permission required. Opening system settings...", isError = true)
                MosesModRepository.requestOverlayPermission(app)
            }
        }
    }

    fun hasOverlayPermission(): Boolean {
        return MosesModRepository.hasOverlayPermission(getApplication())
    }

    fun requestOverlayPermission() {
        MosesModRepository.requestOverlayPermission(getApplication())
    }

    fun clearLogs() {
        _consoleLogs.value = emptyList()
    }
}
