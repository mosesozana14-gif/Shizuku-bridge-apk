package com.example.bitlife

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import com.example.shizuku.ShizukuManager
import java.io.File
import java.nio.charset.StandardCharsets
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
        val ALL_BITLIFE_PACKAGES = listOf(
            "com.candywriter.bitlife",
            "com.goodgamestudios.bitlife.go.life.simulation",
            "com.goodgamestudios.bitlife.de.deutsch.life.simulation",
            "com.goodgamestudios.bitlife.es.espanol.simulador.de.vida",
            "com.goodgamestudios.bitlife.br.portugues.simulacao.de.vida",
            "com.goodgamestudios.bitlife.fr.francais.simulation.de.vie",
            "com.candywriter.bitlifebr",
            "com.candywriter.bitlifede",
            "com.candywriter.bitlifees",
            "com.candywriter.bitlifefr",
            "com.candywriter.bitlifeit",
            "com.candywriter.doglife",
            "com.candywriter.catlife"
        )
        val SEARCH_PATHS = listOf(
            "/storage/emulated/0/Android/data/$BITLIFE_PACKAGE/files",
            "/data/media/0/Android/data/$BITLIFE_PACKAGE/files",
            "/sdcard/Android/data/$BITLIFE_PACKAGE/files",
            "/mnt/user/0/emulated/0/Android/data/$BITLIFE_PACKAGE/files",
            "/mnt/runtime/default/emulated/0/Android/data/$BITLIFE_PACKAGE/files",
            "/data/data/$BITLIFE_PACKAGE/files",
            "/data/user/0/$BITLIFE_PACKAGE/files"
        )

        val SUPPORTED_GAMES = listOf(
            SupportedGame(
                id = "bitlife",
                name = "BitLife - Life Simulator",
                packageName = BITLIFE_PACKAGE,
                iconName = "bitlife",
                isAvailable = true,
                description = "God Mode, infinite cash, 100% stats, social media & in-game cheats"
            ),
            SupportedGame(
                id = "bitlife_go",
                name = "BitLife GO",
                packageName = "com.goodgamestudios.bitlife.go.life.simulation",
                iconName = "bitlife",
                isAvailable = true,
                description = "BitLife GO Edition (GoodGame Studios)"
            ),
            SupportedGame(
                id = "bitlife_de_gg",
                name = "BitLife DE (GoodGame)",
                packageName = "com.goodgamestudios.bitlife.de.deutsch.life.simulation",
                iconName = "bitlife",
                isAvailable = true,
                description = "BitLife German Edition"
            ),
            SupportedGame(
                id = "bitlife_es_gg",
                name = "BitLife ES (GoodGame)",
                packageName = "com.goodgamestudios.bitlife.es.espanol.simulador.de.vida",
                iconName = "bitlife",
                isAvailable = true,
                description = "BitLife Spanish Edition"
            ),
            SupportedGame(
                id = "bitlife_br_gg",
                name = "BitLife BR (GoodGame)",
                packageName = "com.goodgamestudios.bitlife.br.portugues.simulacao.de.vida",
                iconName = "bitlife",
                isAvailable = true,
                description = "BitLife Brazilian / Portuguese Edition"
            ),
            SupportedGame(
                id = "bitlife_fr_gg",
                name = "BitLife FR (GoodGame)",
                packageName = "com.goodgamestudios.bitlife.fr.francais.simulation.de.vie",
                iconName = "bitlife",
                isAvailable = true,
                description = "BitLife French Edition"
            ),
            SupportedGame(
                id = "bitlife_br",
                name = "BitLife BR / PT (Candywriter)",
                packageName = "com.candywriter.bitlifebr",
                iconName = "bitlife",
                isAvailable = true,
                description = "BitLife Brazilian Edition"
            ),
            SupportedGame(
                id = "doglife",
                name = "DogLife",
                packageName = "com.candywriter.doglife",
                iconName = "doglife",
                isAvailable = true,
                description = "DogLife Pet Life Simulator"
            ),
            SupportedGame(
                id = "catlife",
                name = "CatLife",
                packageName = "com.candywriter.catlife",
                iconName = "catlife",
                isAvailable = true,
                description = "CatLife Pet Life Simulator"
            )
        )
    }

    private val _selectedGame = MutableStateFlow(SUPPORTED_GAMES.first())
    val selectedGame: StateFlow<SupportedGame> = _selectedGame.asStateFlow()

    fun selectGame(game: SupportedGame) {
        _selectedGame.value = game
    }

    private var _cachedPickedBytes: ByteArray? = null

    fun isZArchiverInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo("ru.zdevs.zarchiver", 0)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun launchZArchiver(): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage("ru.zdevs.zarchiver")
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    fun launchBitLifeApp(): Boolean {
        val packagesToTry = listOf(_selectedGame.value.packageName) + ALL_BITLIFE_PACKAGES
        for (pkg in packagesToTry.distinct()) {
            try {
                val intent = context.packageManager.getLaunchIntentForPackage(pkg)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return true
                }
            } catch (_: Exception) {}
        }
        return false
    }

    suspend fun autoInjectNewGodLife(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        _isBusy.value = true
        try {
            val targetPkg = _selectedGame.value.packageName
            val targetBaseDir = _scanResult.value.baseDirFound.ifEmpty {
                "/storage/emulated/0/Android/data/$targetPkg/files"
            }

            // 1. Create base directories & protected LiveDictionary
            shizukuManager.executePrivilegedCommand("mkdir", "-p", targetBaseDir)
            shizukuManager.executePrivilegedCommand("mkdir", "-p", "$targetBaseDir/LiveDictionary")
            shizukuManager.executePrivilegedCommand("chmod", "770", "$targetBaseDir/LiveDictionary")
            shizukuManager.executePrivilegedCommand("mkdir", "-p", "$targetBaseDir/sg1")

            // 2. Generate and write authentic Apollo MonetizationVars
            val godBytes = BitLifeSavePatcher.generateGodModeMonetizationVars()
            val base64God = Base64.encodeToString(godBytes, Base64.NO_WRAP)
            val monPath = "$targetBaseDir/MonetizationVars"
            val writeMon = shizukuManager.executePrivilegedCommand("sh", "-c", "echo '$base64God' | base64 -d > '$monPath'")
            shizukuManager.executePrivilegedCommand("chmod", "660", monPath)

            // 3. Generate clean starting save file ($0 cash, 100% core attributes, 18 yo, mods default OFF)
            val initialStats = BitLifeStats(
                happiness = 100,
                health = 100,
                smarts = 100,
                looks = 100,
                karma = 50,
                fame = 0,
                athleticism = 50,
                discipline = 50,
                willpower = 50,
                jobPerformance = 50,
                schoolGrades = 50,
                age = 18,
                ageAtDeath = 100,
                bankBalance = 0L,
                salary = 0L,
                socialStats = BitLifeSocialStats(),
                powerUps = BitLifePowerUps()
            )
            val saveBytes = BitLifeSavePatcher.patchStats(ByteArray(0), initialStats)
            val base64Save = Base64.encodeToString(saveBytes, Base64.NO_WRAP)
            
            // Write to both root savedLife.data and sg1/savedLife.data
            val rootSavePath = "$targetBaseDir/savedLife.data"
            val sg1SavePath = "$targetBaseDir/sg1/savedLife.data"
            shizukuManager.executePrivilegedCommand("sh", "-c", "echo '$base64Save' | base64 -d > '$rootSavePath'")
            shizukuManager.executePrivilegedCommand("chmod", "660", rootSavePath)
            shizukuManager.executePrivilegedCommand("sh", "-c", "echo '$base64Save' | base64 -d > '$sg1SavePath'")
            shizukuManager.executePrivilegedCommand("chmod", "660", sg1SavePath)

            // Also patch existing selected slot if present
            val selected = _selectedSlot.value
            if (selected != null && selected.filePath.isNotBlank()) {
                val writeExisting = shizukuManager.executePrivilegedCommand("sh", "-c", "echo '$base64Save' | base64 -d > '${selected.filePath}'")
                shizukuManager.executePrivilegedCommand("chmod", "660", selected.filePath)
            }

            shizukuManager.executePrivilegedCommand("sync")

            // Re-scan and auto-load the newly injected life into the editor
            _currentStats.value = initialStats
            scanBitLifeFiles()

            _isBusy.value = false
            if (writeMon.isSuccess) {
                Pair(true, "⚡ Automatically Injected God Mode ($10B Cash, 100% Stats, All DLCs & Expansions) directly into BitLife!")
            } else {
                Pair(false, "Could not write to BitLife directory: ${writeMon.error.ifEmpty { writeMon.output }}")
            }
        } catch (e: Exception) {
            _isBusy.value = false
            Pair(false, "Error auto-injecting into BitLife: ${e.localizedMessage ?: e.message}")
        }
    }

    suspend fun loadPickedFile(uri: Uri): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        _isBusy.value = true
        try {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (_: Exception) {}

            var fileName = "savedLife.data"
            var fileSize = 0L

            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIdx != -1) fileName = cursor.getString(nameIdx) ?: fileName
                    if (sizeIdx != -1) fileSize = cursor.getLong(sizeIdx)
                }
            }

            val rawBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (rawBytes == null || rawBytes.isEmpty()) {
                _isBusy.value = false
                return@withContext Pair(false, "Selected file is empty or could not be opened.")
            }

            _cachedPickedBytes = rawBytes

            val isMonetization = fileName.contains("MonetizationVars", ignoreCase = true) ||
                    (rawBytes.size < 4000 && String(rawBytes, StandardCharsets.UTF_8).contains("Bitizen", ignoreCase = true))

            val slot: BitLifeSlotInfo
            if (isMonetization) {
                slot = BitLifeSlotInfo(
                    slotName = "MonetizationVars (ZArchiver)",
                    filePath = uri.toString(),
                    contentUri = uri,
                    fileSize = rawBytes.size.toLong(),
                    characterSummary = "DLC & Purchases Configuration File",
                    isPrimaryActive = true,
                    isPickedFile = true,
                    isMonetizationVars = true
                )
            } else {
                val parsed = try {
                    BitLifeSavePatcher.parseStats(rawBytes)
                } catch (_: Exception) {
                    BitLifeStats()
                }
                _currentStats.value = parsed
                val summary = "Age ${parsed.age} • $${formatCompactNumber(parsed.bankBalance)} • ${parsed.health}% Health"
                slot = BitLifeSlotInfo(
                    slotName = "$fileName (ZArchiver)",
                    filePath = uri.toString(),
                    contentUri = uri,
                    fileSize = rawBytes.size.toLong(),
                    characterSummary = summary,
                    isPrimaryActive = true,
                    isPickedFile = true,
                    isMonetizationVars = false
                )
            }

            val currentList = _scanResult.value.availableSlots.toMutableList()
            currentList.removeAll { it.contentUri == uri || it.filePath == slot.filePath }
            currentList.add(0, slot)

            _scanResult.value = _scanResult.value.copy(
                availableSlots = currentList,
                error = ""
            )
            _selectedSlot.value = slot
            _isBusy.value = false
            Pair(true, "Loaded '$fileName' from ZArchiver / Storage! Character data is ready to edit.")
        } catch (e: Exception) {
            _isBusy.value = false
            Pair(false, "Failed to load picked file: ${e.localizedMessage ?: e.message}")
        }
    }

    suspend fun exportPatchedSave(targetUri: Uri, newStats: BitLifeStats): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        _isBusy.value = true
        try {
            val originalBytes = _cachedPickedBytes ?: ByteArray(0)
            val patchedBytes = BitLifeSavePatcher.patchStats(originalBytes, newStats)
            context.contentResolver.openOutputStream(targetUri, "wt")?.use { os ->
                os.write(patchedBytes)
                os.flush()
            }
            _cachedPickedBytes = patchedBytes
            _currentStats.value = newStats
            _isBusy.value = false
            Pair(true, "Patched save file exported successfully! You can copy/paste it with ZArchiver into your BitLife folder.")
        } catch (e: Exception) {
            _isBusy.value = false
            Pair(false, "Failed to export save: ${e.localizedMessage ?: e.message}")
        }
    }

    suspend fun exportGodModeMonetization(targetUri: Uri): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        _isBusy.value = true
        try {
            val godBytes = BitLifeSavePatcher.generateGodModeMonetizationVars()
            context.contentResolver.openOutputStream(targetUri, "wt")?.use { os ->
                os.write(godBytes)
                os.flush()
            }
            _isBusy.value = false
            Pair(true, "God Mode & DLC MonetizationVars exported! Copy it with ZArchiver into com.candywriter.bitlife/files/")
        } catch (e: Exception) {
            _isBusy.value = false
            Pair(false, "Failed to export MonetizationVars: ${e.localizedMessage ?: e.message}")
        }
    }

    suspend fun scanBitLifeFiles(): BitLifeSaveScanResult = withContext(Dispatchers.IO) {
        _isBusy.value = true
        var foundBaseDir = ""
        val detectedSlots = mutableListOf<BitLifeSlotInfo>()
        var hasMonetization = false

        // Preserve any user-picked files from ZArchiver/SAF
        val pickedSlots = _scanResult.value.availableSlots.filter { it.isPickedFile }
        detectedSlots.addAll(pickedSlots)

        val packagesToScan = listOf(
            _selectedGame.value.packageName,
            "com.candywriter.bitlife",
            "com.candywriter.bitlifebr",
            "com.candywriter.bitlifede",
            "com.candywriter.bitlifees",
            "com.candywriter.bitlifefr",
            "com.candywriter.bitlifeit"
        ).distinct()

        // 1. Verify package installation via PM and Shizuku
        var isInstalled = false
        var installedPackage = _selectedGame.value.packageName

        for (pkg in packagesToScan) {
            val pmInstalled = try {
                context.packageManager.getPackageInfo(pkg, 0)
                true
            } catch (_: Exception) {
                val pkgRes = shizukuManager.executePrivilegedCommand("pm", "path", pkg)
                pkgRes.isSuccess && pkgRes.output.contains("package:")
            }
            if (pmInstalled) {
                isInstalled = true
                installedPackage = pkg
                break
            }
        }

        // 2. Deep Automatic Shell Finder: Search all filesystems and mounts
        val deepFindRes = shizukuManager.executePrivilegedCommand(
            "sh", "-c",
            "find /storage/emulated/0/Android/data /data/media/0/Android/data /sdcard/Android/data /mnt/user/0/emulated/0/Android/data /storage/emulated/0/Download /sdcard/Download -name '*savedLife*.data' -o -name 'MonetizationVars' 2>/dev/null"
        )

        val discoveredPaths = mutableSetOf<String>()
        if (deepFindRes.isSuccess && deepFindRes.output.isNotBlank()) {
            deepFindRes.output.lines().forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isNotBlank() && !trimmed.contains("No such file") && !trimmed.contains("Permission denied")) {
                    discoveredPaths.add(trimmed)
                }
            }
        }

        // Build comprehensive candidate directory list
        val candidateDirs = mutableListOf<String>()
        for (pkg in packagesToScan) {
            candidateDirs.add("/storage/emulated/0/Android/data/$pkg/files")
            candidateDirs.add("/data/media/0/Android/data/$pkg/files")
            candidateDirs.add("/sdcard/Android/data/$pkg/files")
            candidateDirs.add("/mnt/user/0/emulated/0/Android/data/$pkg/files")
            candidateDirs.add("/mnt/runtime/default/emulated/0/Android/data/$pkg/files")
            candidateDirs.add("/data/data/$pkg/files")
            candidateDirs.add("/data/user/0/$pkg/files")
        }

        // Extract baseDir from discovered files if available
        for (filePath in discoveredPaths) {
            if (filePath.contains("com.candywriter.bitlife") || filePath.contains("bitlife")) {
                val dir = filePath.substringBeforeLast("/")
                if (dir.endsWith("/files") || dir.contains("/files/")) {
                    val rootFiles = if (dir.endsWith("/files")) dir else dir.substringBefore("/files") + "/files"
                    foundBaseDir = rootFiles
                    break
                }
            }
        }

        // Fallback directory search if find didn't set foundBaseDir
        if (foundBaseDir.isEmpty()) {
            for (basePath in candidateDirs) {
                val dir = File(basePath)
                if (dir.exists() && dir.canRead()) {
                    foundBaseDir = basePath
                    break
                }
                val checkDir = shizukuManager.executePrivilegedCommand("ls", "-d", basePath)
                if (checkDir.isSuccess && !checkDir.output.contains("No such file")) {
                    foundBaseDir = basePath
                    break
                }
            }
        }

        // Process found base directory
        if (foundBaseDir.isNotEmpty()) {
            // Check MonetizationVars
            val monCheck = shizukuManager.executePrivilegedCommand("ls", "$foundBaseDir/MonetizationVars")
            hasMonetization = monCheck.isSuccess && !monCheck.output.contains("No such file")

            // Scan all possible slot directories: sg* (sg1, sg2, sg3...), slot*, saves*, etc.
            val listSlots = shizukuManager.executePrivilegedCommand("sh", "-c", "ls -d $foundBaseDir/sg* $foundBaseDir/slot* 2>/dev/null")
            if (listSlots.isSuccess && listSlots.output.isNotBlank()) {
                val lines = listSlots.output.lines()
                for (line in lines) {
                    val dirPath = line.trim()
                    if (dirPath.isBlank() || dirPath.contains("No such file")) continue
                    val slotName = dirPath.substringAfterLast("/")
                    val primarySavePath = "$dirPath/savedLife.data"

                    val checkSave = shizukuManager.executePrivilegedCommand("ls", "-la", primarySavePath)
                    val exists = checkSave.isSuccess && !checkSave.output.contains("No such file")

                    val ageFilesRes = shizukuManager.executePrivilegedCommand("sh", "-c", "ls $dirPath/savedLife-age*.data 2>/dev/null")
                    val ageFiles = if (ageFilesRes.isSuccess) {
                        ageFilesRes.output.lines().filter { it.isNotBlank() && !it.contains("No such file") }
                    } else emptyList()

                    if (exists || ageFiles.isNotEmpty()) {
                        var summary = "Character Slot ($slotName)"
                        var fSize = 0L
                        val targetToInspect = if (exists) primarySavePath else ageFiles.firstOrNull() ?: ""
                        if (targetToInspect.isNotEmpty()) {
                            val headRes = shizukuManager.executePrivilegedCommand("base64", targetToInspect)
                            if (headRes.isSuccess && headRes.output.isNotBlank()) {
                                try {
                                    val clean = headRes.output.replace("\n", "").replace("\r", "")
                                    val raw = Base64.decode(clean, Base64.DEFAULT)
                                    fSize = raw.size.toLong()
                                    val pStats = BitLifeSavePatcher.parseStats(raw)
                                    summary = "Age ${pStats.age} • $${formatCompactNumber(pStats.bankBalance)} • ${pStats.health}% Health"
                                } catch (_: Exception) {}
                            }
                        }

                        detectedSlots.add(
                            BitLifeSlotInfo(
                                slotName = "Slot $slotName",
                                filePath = primarySavePath,
                                ageDataFiles = ageFiles,
                                fileSize = fSize,
                                characterSummary = summary,
                                isPrimaryActive = detectedSlots.isEmpty()
                            )
                        )
                    }
                }
            }

            // Check root savedLife.data as well
            val rootSave = shizukuManager.executePrivilegedCommand("ls", "-la", "$foundBaseDir/savedLife.data")
            if (rootSave.isSuccess && !rootSave.output.contains("No such file")) {
                var summary = "Active Life (Root)"
                var fSize = 0L
                val headRes = shizukuManager.executePrivilegedCommand("base64", "$foundBaseDir/savedLife.data")
                if (headRes.isSuccess && headRes.output.isNotBlank()) {
                    try {
                        val clean = headRes.output.replace("\n", "").replace("\r", "")
                        val raw = Base64.decode(clean, Base64.DEFAULT)
                        fSize = raw.size.toLong()
                        val pStats = BitLifeSavePatcher.parseStats(raw)
                        summary = "Age ${pStats.age} • $${formatCompactNumber(pStats.bankBalance)} • ${pStats.health}% Health"
                    } catch (_: Exception) {}
                }

                detectedSlots.add(
                    0, // Prioritize root save if present
                    BitLifeSlotInfo(
                        slotName = "Main Save (Active Life)",
                        filePath = "$foundBaseDir/savedLife.data",
                        fileSize = fSize,
                        characterSummary = summary,
                        isPrimaryActive = true
                    )
                )
            }
        }

        // Check discovered paths from Download/Documents folders if still empty
        for (discPath in discoveredPaths) {
            if (discPath.endsWith("savedLife.data") && detectedSlots.none { it.filePath == discPath }) {
                var summary = "Discovered Save (${discPath.substringAfterLast("/")})"
                var fSize = 0L
                val headRes = shizukuManager.executePrivilegedCommand("base64", discPath)
                if (headRes.isSuccess && headRes.output.isNotBlank()) {
                    try {
                        val clean = headRes.output.replace("\n", "").replace("\r", "")
                        val raw = Base64.decode(clean, Base64.DEFAULT)
                        fSize = raw.size.toLong()
                        val pStats = BitLifeSavePatcher.parseStats(raw)
                        summary = "Age ${pStats.age} • $${formatCompactNumber(pStats.bankBalance)} • ${pStats.health}% Health"
                    } catch (_: Exception) {}
                }
                detectedSlots.add(
                    BitLifeSlotInfo(
                        slotName = "Auto-Discovered Save",
                        filePath = discPath,
                        fileSize = fSize,
                        characterSummary = summary,
                        isPrimaryActive = detectedSlots.isEmpty()
                    )
                )
            }
        }

        // Direct Java File check for accessible Download directories
        val downloadDirs = listOf(
            File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS), "savedLife.data"),
            File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS), "BitLife/savedLife.data")
        )
        for (f in downloadDirs) {
            if (f.exists() && f.canRead() && detectedSlots.none { it.filePath == f.absolutePath }) {
                detectedSlots.add(
                    BitLifeSlotInfo(
                        slotName = "Downloads (${f.name})",
                        filePath = f.absolutePath,
                        fileSize = f.length(),
                        characterSummary = "Local Downloaded Save",
                        isPrimaryActive = detectedSlots.isEmpty()
                    )
                )
            }
        }

        val result = BitLifeSaveScanResult(
            isAppInstalled = isInstalled,
            baseDirFound = foundBaseDir,
            availableSlots = detectedSlots,
            hasMonetizationVars = hasMonetization,
            error = if (foundBaseDir.isEmpty() && !isInstalled) "BitLife is not detected on this device. Please install BitLife first." else ""
        )

        _scanResult.value = result

        // Auto-select and Auto-load active slot automatically!
        val currentSel = _selectedSlot.value
        if (currentSel == null || detectedSlots.none { it.filePath == currentSel.filePath }) {
            if (detectedSlots.isNotEmpty()) {
                val bestSlot = detectedSlots.first()
                _selectedSlot.value = bestSlot
                loadStatsFromSlot(bestSlot)
            } else {
                _selectedSlot.value = null
            }
        }

        _isBusy.value = false
        result
    }

    private fun formatCompactNumber(amount: Long): String {
        return when {
            amount >= 1_000_000_000_000L -> String.format("%.1fT", amount / 1_000_000_000_000.0)
            amount >= 1_000_000_000L -> String.format("%.1fB", amount / 1_000_000_000.0)
            amount >= 1_000_000L -> String.format("%.1fM", amount / 1_000_000.0)
            amount >= 1_000L -> String.format("%.1fK", amount / 1_000.0)
            else -> amount.toString()
        }
    }

    fun selectSlot(slot: BitLifeSlotInfo) {
        _selectedSlot.value = slot
    }

    suspend fun loadStatsFromSlot(slot: BitLifeSlotInfo): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        _isBusy.value = true

        if (slot.contentUri != null) {
            return@withContext try {
                val rawBytes = context.contentResolver.openInputStream(slot.contentUri)?.use { it.readBytes() }
                if (rawBytes == null || rawBytes.isEmpty()) {
                    _isBusy.value = false
                    Pair(false, "Picked file is empty or could not be opened.")
                } else {
                    _cachedPickedBytes = rawBytes
                    val parsedStats = BitLifeSavePatcher.parseStats(rawBytes)
                    _currentStats.value = parsedStats
                    _isBusy.value = false
                    Pair(true, "Loaded stats successfully from ${slot.slotName}")
                }
            } catch (e: Exception) {
                _isBusy.value = false
                Pair(false, "Error reading picked file: ${e.localizedMessage ?: e.message}")
            }
        }

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

        // 1. If loaded via ZArchiver / SAF Content URI
        if (slot.contentUri != null) {
            return@withContext try {
                val uri = slot.contentUri
                val originalBytes = _cachedPickedBytes ?: run {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
                }
                val patchedBytes = BitLifeSavePatcher.patchStats(originalBytes, newStats)
                context.contentResolver.openOutputStream(uri, "wt")?.use { os ->
                    os.write(patchedBytes)
                    os.flush()
                }
                _cachedPickedBytes = patchedBytes
                _currentStats.value = newStats
                _isBusy.value = false
                Pair(true, "Changes saved directly to ${slot.slotName}! Resume BitLife to continue your life.")
            } catch (e: Exception) {
                _isBusy.value = false
                Pair(false, "Could not write directly to file: ${e.localizedMessage ?: e.message}. Use 'Export Patched Save' to save a modified copy for ZArchiver!")
            }
        }

        // 2. Otherwise use Shizuku privileged shell execution
        val timestamp = System.currentTimeMillis()
        val backupPath = "${slot.filePath}.bak_$timestamp"
        shizukuManager.executePrivilegedCommand("cp", slot.filePath, backupPath)

        // Read current file bytes
        val readRes = shizukuManager.executePrivilegedCommand("base64", slot.filePath)
        val rawBytes = if (readRes.isSuccess && readRes.output.isNotBlank()) {
            try {
                val clean = readRes.output.replace("\n", "").replace("\r", "")
                Base64.decode(clean, Base64.DEFAULT)
            } catch (_: Exception) { ByteArray(0) }
        } else {
            ByteArray(0)
        }

        // Patch the bytes with new stats
        val patchedBytes = BitLifeSavePatcher.patchStats(rawBytes, newStats)
        val base64Patched = Base64.encodeToString(patchedBytes, Base64.NO_WRAP)

        // Write back to disk using base64 decode pipe
        val writeCmd = "echo '$base64Patched' | base64 -d > '${slot.filePath}'"
        val writeRes = shizukuManager.executePrivilegedCommand(writeCmd)

        // Ensure correct file permissions
        shizukuManager.executePrivilegedCommand("chmod", "660", slot.filePath)
        shizukuManager.executePrivilegedCommand("sync")

        // Optionally patch sub-age files
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
        val selected = _selectedSlot.value
        if (selected != null && selected.contentUri != null && selected.isMonetizationVars) {
            _isBusy.value = true
            return@withContext try {
                val godBytes = BitLifeSavePatcher.generateGodModeMonetizationVars()
                context.contentResolver.openOutputStream(selected.contentUri, "wt")?.use { os ->
                    os.write(godBytes)
                    os.flush()
                }
                _isBusy.value = false
                Pair(true, "God Mode, Bitizenship & All Packs written directly to ${selected.slotName}!")
            } catch (e: Exception) {
                _isBusy.value = false
                Pair(false, "Could not write MonetizationVars: ${e.localizedMessage ?: e.message}")
            }
        }

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
        
        // Prepare LiveDictionary folder to protect MonetizationVars from being overwritten/reset by BitLife
        shizukuManager.executePrivilegedCommand("mkdir", "-p", "$baseDir/LiveDictionary")
        shizukuManager.executePrivilegedCommand("chmod", "770", "$baseDir/LiveDictionary")
        
        shizukuManager.executePrivilegedCommand("sync")

        _isBusy.value = false
        if (res.isSuccess) {
            Pair(true, "God Mode, Bitizenship & All Expansion Packs unlocked successfully! (LiveDictionary protected)")
        } else {
            Pair(false, "Failed to write MonetizationVars: ${res.error.ifEmpty { res.output }}")
        }
    }
}
