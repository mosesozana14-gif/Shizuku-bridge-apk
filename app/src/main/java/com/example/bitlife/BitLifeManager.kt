package com.example.bitlife

import android.content.Context
import android.util.Base64
import com.example.shizuku.ShizukuManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class BitLifeManager(
    private val context: Context,
    private val shizukuManager: ShizukuManager
) {
    private val _scanResult = MutableStateFlow(BitLifeSaveScanResult())
    val scanResult: StateFlow<BitLifeSaveScanResult> = _scanResult.asStateFlow()

    private val _currentStats = MutableStateFlow(BitLifeStats())
    val currentStats: StateFlow<BitLifeStats> = _currentStats.asStateFlow()

    private val _selectedSlot = MutableStateFlow<BitLifeSlotInfo?>(null)
    val selectedSlot: StateFlow<BitLifeSlotInfo?> = _selectedSlot.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    companion object {
        const val BITLIFE_PACKAGE = "com.candywriter.bitlife"
        val SEARCH_PATHS = listOf(
            "/storage/emulated/0/Android/data/$BITLIFE_PACKAGE/files",
            "/sdcard/Android/data/$BITLIFE_PACKAGE/files",
            "/data/data/$BITLIFE_PACKAGE/files"
        )
    }

    suspend fun scanBitLifeFiles(): BitLifeSaveScanResult = withContext(Dispatchers.IO) {
        _isBusy.value = true
        var foundBaseDir = ""
        val detectedSlots = mutableListOf<BitLifeSlotInfo>()
        var hasMonetization = false

        // 1. Verify package installation
        val pkgRes = shizukuManager.executePrivilegedCommand("pm", "path", BITLIFE_PACKAGE)
        val isInstalled = pkgRes.isSuccess && pkgRes.output.contains("package:")

        for (basePath in SEARCH_PATHS) {
            val checkDir = shizukuManager.executePrivilegedCommand("ls", "-d", basePath)
            if (checkDir.isSuccess && !checkDir.output.contains("No such file")) {
                foundBaseDir = basePath
                break
            }
        }

        if (foundBaseDir.isNotEmpty()) {
            // Check MonetizationVars
            val monCheck = shizukuManager.executePrivilegedCommand("ls", "$foundBaseDir/MonetizationVars")
            hasMonetization = monCheck.isSuccess && !monCheck.output.contains("No such file")

            // Check root savedLife.data
            val rootSave = shizukuManager.executePrivilegedCommand("ls", "-la", "$foundBaseDir/savedLife.data")
            if (rootSave.isSuccess && !rootSave.output.contains("No such file")) {
                detectedSlots.add(
                    BitLifeSlotInfo(
                        slotName = "Main Save (Root)",
                        filePath = "$foundBaseDir/savedLife.data"
                    )
                )
            }

            // Check sg* directories (sg1, sg2, sg3, sg4, sg5...)
            val listSlots = shizukuManager.executePrivilegedCommand("ls", "-d", "$foundBaseDir/sg*")
            if (listSlots.isSuccess && listSlots.output.isNotBlank()) {
                val lines = listSlots.output.lines()
                for (line in lines) {
                    val dirPath = line.trim()
                    if (dirPath.isBlank()) continue
                    val slotName = dirPath.substringAfterLast("/")
                    val primarySavePath = "$dirPath/savedLife.data"

                    // Find all age files in this slot
                    val ageFilesRes = shizukuManager.executePrivilegedCommand("ls", "$dirPath/savedLife-age*.data")
                    val ageFiles = if (ageFilesRes.isSuccess) {
                        ageFilesRes.output.lines().filter { it.isNotBlank() }
                    } else emptyList()

                    detectedSlots.add(
                        BitLifeSlotInfo(
                            slotName = "Slot $slotName",
                            filePath = primarySavePath,
                            ageDataFiles = ageFiles
                        )
                    )
                }
            }
        }

        val result = BitLifeSaveScanResult(
            isAppInstalled = isInstalled,
            baseDirFound = foundBaseDir,
            availableSlots = detectedSlots,
            hasMonetizationVars = hasMonetization,
            error = if (foundBaseDir.isEmpty() && !isInstalled) "BitLife is not detected on this device." else ""
        )

        _scanResult.value = result
        if (detectedSlots.isNotEmpty() && _selectedSlot.value == null) {
            _selectedSlot.value = detectedSlots.first()
            loadStatsFromSlot(detectedSlots.first())
        }

        _isBusy.value = false
        result
    }

    fun selectSlot(slot: BitLifeSlotInfo) {
        _selectedSlot.value = slot
    }

    suspend fun loadStatsFromSlot(slot: BitLifeSlotInfo): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        _isBusy.value = true

        // Read file using base64 command to avoid encoding/corrupted bytes
        val res = shizukuManager.executePrivilegedCommand("base64", slot.filePath)
        if (!res.isSuccess || res.output.isBlank()) {
            _isBusy.value = false
            return@withContext Pair(false, "Failed to read save data at ${slot.filePath}: ${res.error.ifEmpty { "File empty or unreadable" }}")
        }

        try {
            val cleanBase64 = res.output.replace("\n", "").replace("\r", "")
            val rawBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            val parsedStats = BitLifeSavePatcher.parseStats(rawBytes)
            _currentStats.value = parsedStats
            _isBusy.value = false
            Pair(true, "Loaded stats successfully from ${slot.slotName}")
        } catch (e: Exception) {
            _isBusy.value = false
            Pair(false, "Error parsing save file: ${e.message}")
        }
    }

    suspend fun saveAndApplyStats(
        slot: BitLifeSlotInfo,
        newStats: BitLifeStats,
        alsoUpdateAgeFiles: Boolean = true
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        _isBusy.value = true

        // 1. Create a safe backup first
        val timestamp = System.currentTimeMillis()
        val backupPath = "${slot.filePath}.bak_$timestamp"
        shizukuManager.executePrivilegedCommand("cp", slot.filePath, backupPath)

        // 2. Read current file bytes
        val readRes = shizukuManager.executePrivilegedCommand("base64", slot.filePath)
        val rawBytes = if (readRes.isSuccess && readRes.output.isNotBlank()) {
            try {
                val clean = readRes.output.replace("\n", "").replace("\r", "")
                Base64.decode(clean, Base64.DEFAULT)
            } catch (_: Exception) { ByteArray(0) }
        } else {
            ByteArray(0)
        }

        // 3. Patch the bytes with new stats
        val patchedBytes = BitLifeSavePatcher.patchStats(rawBytes, newStats)
        val base64Patched = Base64.encodeToString(patchedBytes, Base64.NO_WRAP)

        // 4. Write back to disk using base64 decode pipe
        val writeCmd = "echo '$base64Patched' | base64 -d > '${slot.filePath}'"
        val writeRes = shizukuManager.executePrivilegedCommand(writeCmd)

        // 5. Ensure correct file permissions
        shizukuManager.executePrivilegedCommand("chmod", "660", slot.filePath)
        shizukuManager.executePrivilegedCommand("sync")

        // 6. Optionally patch sub-age files (savedLife-age*.data) so loading an older autosave also keeps the max stats
        if (alsoUpdateAgeFiles && slot.ageDataFiles.isNotEmpty()) {
            for (ageFile in slot.ageDataFiles) {
                val ageCmd = "echo '$base64Patched' | base64 -d > '$ageFile'"
                shizukuManager.executePrivilegedCommand(ageCmd)
                shizukuManager.executePrivilegedCommand("chmod", "660", ageFile)
            }
        }

        _currentStats.value = newStats
        _isBusy.value = false

        if (writeRes.isSuccess) {
            Pair(true, "Stats saved & applied successfully! Backup created: savedLife.data.bak_$timestamp")
        } else {
            Pair(false, "Write command returned code ${writeRes.exitCode}: ${writeRes.error.ifEmpty { writeRes.output }}")
        }
    }

    suspend fun unlockGodModeAndMonetization(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val baseDir = _scanResult.value.baseDirFound.ifEmpty {
            "/storage/emulated/0/Android/data/$BITLIFE_PACKAGE/files"
        }

        _isBusy.value = true
        val godBytes = BitLifeSavePatcher.generateGodModeMonetizationVars()
        val base64Str = Base64.encodeToString(godBytes, Base64.NO_WRAP)
        val targetPath = "$baseDir/MonetizationVars"

        // Backup existing if exists
        shizukuManager.executePrivilegedCommand("cp", targetPath, "$targetPath.bak_${System.currentTimeMillis()}")

        val cmd = "echo '$base64Str' | base64 -d > '$targetPath'"
        val res = shizukuManager.executePrivilegedCommand(cmd)
        shizukuManager.executePrivilegedCommand("chmod", "660", targetPath)
        shizukuManager.executePrivilegedCommand("sync")

        _isBusy.value = false
        if (res.isSuccess) {
            Pair(true, "God Mode, Bitizenship & All Expansion Packs unlocked successfully!")
        } else {
            Pair(false, "Failed to write MonetizationVars: ${res.error.ifEmpty { res.output }}")
        }
    }
}
