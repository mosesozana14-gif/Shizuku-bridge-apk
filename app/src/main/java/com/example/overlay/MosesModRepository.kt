package com.example.overlay

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.example.bitlife.BitLifeManager
import com.example.bitlife.BitLifePowerUps
import com.example.bitlife.BitLifeSlotInfo
import com.example.bitlife.BitLifeSocialStats
import com.example.bitlife.BitLifeStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object MosesModRepository {

    private val _isOverlayActive = MutableStateFlow(false)
    val isOverlayActive: StateFlow<Boolean> = _isOverlayActive.asStateFlow()

    private val _isHudExpanded = MutableStateFlow(false)
    val isHudExpanded: StateFlow<Boolean> = _isHudExpanded.asStateFlow()

    private val _activeStats = MutableStateFlow(BitLifeStats())
    val activeStats: StateFlow<BitLifeStats> = _activeStats.asStateFlow()

    private val _selectedSlot = MutableStateFlow<BitLifeSlotInfo?>(null)
    val selectedSlot: StateFlow<BitLifeSlotInfo?> = _selectedSlot.asStateFlow()

    private val _overlayMessage = MutableStateFlow("")
    val overlayMessage: StateFlow<String> = _overlayMessage.asStateFlow()

    private var bitLifeManager: BitLifeManager? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    fun initialize(manager: BitLifeManager) {
        this.bitLifeManager = manager
        scope.launch {
            manager.currentStats.collect { stats ->
                _activeStats.value = stats
            }
        }
        scope.launch {
            manager.selectedSlot.collect { slot ->
                _selectedSlot.value = slot
            }
        }
    }

    fun setOverlayActive(active: Boolean) {
        _isOverlayActive.value = active
    }

    fun toggleHud() {
        _isHudExpanded.value = !_isHudExpanded.value
    }

    fun setHudExpanded(expanded: Boolean) {
        _isHudExpanded.value = expanded
    }

    fun updateStats(newStats: BitLifeStats) {
        _activeStats.value = newStats
    }

    fun updateSocialStats(transform: (BitLifeSocialStats) -> BitLifeSocialStats) {
        _activeStats.value = _activeStats.value.copy(
            socialStats = transform(_activeStats.value.socialStats)
        )
    }

    fun updatePowerUps(transform: (BitLifePowerUps) -> BitLifePowerUps) {
        _activeStats.value = _activeStats.value.copy(
            powerUps = transform(_activeStats.value.powerUps)
        )
    }

    fun setMessage(msg: String) {
        _overlayMessage.value = msg
    }

    fun applyToBitLife(onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        val manager = bitLifeManager ?: run {
            onComplete(false, "BitLife Manager not attached")
            return
        }
        val slot = _selectedSlot.value ?: run {
            onComplete(false, "No active save slot selected")
            return
        }

        scope.launch {
            val (success, msg) = manager.saveAndApplyStats(slot, _activeStats.value)
            _overlayMessage.value = msg
            onComplete(success, msg)
        }
    }

    fun unlockAllGodModeAndExpansions(onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        val manager = bitLifeManager ?: run {
            onComplete(false, "BitLife Manager not attached")
            return
        }

        scope.launch {
            val (success, msg) = manager.unlockGodModeAndMonetization()
            _overlayMessage.value = msg
            onComplete(success, msg)
        }
    }

    fun hasOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun requestOverlayPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun startOverlayService(context: Context) {
        val intent = Intent(context, MosesModOverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        _isOverlayActive.value = true
    }

    fun stopOverlayService(context: Context) {
        val intent = Intent(context, MosesModOverlayService::class.java)
        context.stopService(intent)
        _isOverlayActive.value = false
        _isHudExpanded.value = false
    }
}
