package com.example.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.settings.SettingNamespace
import com.example.settings.SystemSettingsStatus
import com.example.shizuku.ShizukuState
import com.example.ui.MainViewModel
import com.example.ui.theme.NaturalBackground
import com.example.ui.theme.NaturalDanger
import com.example.ui.theme.NaturalDangerContainer
import com.example.ui.theme.NaturalOnBackground
import com.example.ui.theme.NaturalOnPrimary
import com.example.ui.theme.NaturalOnPrimaryContainer
import com.example.ui.theme.NaturalOnSurfaceVariant
import com.example.ui.theme.NaturalOutline
import com.example.ui.theme.NaturalOutlineVariant
import com.example.ui.theme.NaturalPrimary
import com.example.ui.theme.NaturalPrimaryContainer
import com.example.ui.theme.NaturalSecondary
import com.example.ui.theme.NaturalSecondaryContainer
import com.example.ui.theme.NaturalSuccess
import com.example.ui.theme.NaturalSuccessContainer
import com.example.ui.theme.NaturalSurface
import com.example.ui.theme.NaturalWarning
import com.example.ui.theme.NaturalWarningContainer

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SystemSettingsTab(
    viewModel: MainViewModel,
    shizukuState: ShizukuState,
    settingsStatus: SystemSettingsStatus,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var customNamespace by remember { mutableStateOf(SettingNamespace.GLOBAL) }
    var customKey by remember { mutableStateOf("") }
    var customValue by remember { mutableStateOf("") }
    var readResult by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NaturalBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(2.dp))
            ShizukuStatusCard(
                state = shizukuState,
                onRequestPermission = { viewModel.requestShizukuPermission() },
                onRefresh = { viewModel.refreshAll() }
            )
        }

        // System Settings Permission Card (Natural Sand Card)
        item {
            SystemSettingsPermissionCard(
                status = settingsStatus,
                shizukuState = shizukuState,
                onGrantViaShizuku = { viewModel.grantSystemSettingsPermissionsViaShizuku() },
                onOpenAndroidSettings = {
                    try {
                        context.startActivity(viewModel.settingsManager.getRequestWriteSettingsIntent())
                    } catch (_: Exception) {}
                }
            )
        }

        // Quick Tweaks Section Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = NaturalPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "System Settings Tweaks",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NaturalOnBackground,
                        fontSize = 17.sp
                    )
                )
            }
        }

        // Animation Scales Tweak Card
        item {
            AnimationScalesCard(
                currentScale = settingsStatus.animatorDurationScale,
                onApplyScale = { viewModel.setAnimationScale(it) }
            )
        }

        // Screen Timeout & Display Card
        item {
            ScreenTimeoutCard(
                currentTimeoutMs = settingsStatus.screenTimeoutMs,
                onApplyTimeout = { viewModel.setScreenTimeout(it) }
            )
        }

        // Refresh Rate & Haptics Card
        item {
            DisplayAndHapticsCard(
                peakRefreshRate = settingsStatus.peakRefreshRate,
                hapticEnabled = settingsStatus.hapticEnabled,
                onSetRefreshRate = { viewModel.setPeakRefreshRate(it) },
                onToggleHaptic = { viewModel.setHapticFeedback(it) }
            )
        }

        // Advanced Key-Value Setting Editor
        item {
            CustomSettingEditorCard(
                selectedNamespace = customNamespace,
                onNamespaceSelected = { customNamespace = it },
                key = customKey,
                onKeyChange = { customKey = it },
                value = customValue,
                onValueChange = { customValue = it },
                onWrite = { viewModel.writeCustomSetting(customNamespace, customKey, customValue) },
                onRead = {
                    viewModel.viewModelScopeLaunchRead(customNamespace, customKey) { res ->
                        readResult = res
                    }
                },
                readResult = readResult
            )
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

// Extension helper for reading custom key
fun MainViewModel.viewModelScopeLaunchRead(
    namespace: SettingNamespace,
    key: String,
    onResult: (String) -> Unit
) {
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).run {
        val result = kotlinx.coroutines.runBlocking {
            settingsManager.readCustomSetting(namespace, key)
        }
        onResult(result)
        log("Read $namespace.$key = $result")
    }
}

@Composable
fun SystemSettingsPermissionCard(
    status: SystemSettingsStatus,
    shizukuState: ShizukuState,
    onGrantViaShizuku: () -> Unit,
    onOpenAndroidSettings: () -> Unit
) {
    val isFullyGranted = status.canWriteSystem && status.hasSecureSettingsPermission
    val hasPartial = status.canWriteSystem || status.hasSecureSettingsPermission

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("system_settings_permission_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = NaturalSecondaryContainer),
        border = androidx.compose.foundation.BorderStroke(1.dp, NaturalOutlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.65f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SettingsSuggest,
                            contentDescription = null,
                            tint = NaturalPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "System Settings",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = NaturalOnBackground
                            )
                        )
                        Text(
                            text = if (isFullyGranted) "Full modify access active" else "Elevation required to edit settings",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = NaturalOnSurfaceVariant
                            )
                        )
                    }
                }

                // Status pill
                Surface(
                    shape = CircleShape,
                    color = if (isFullyGranted) NaturalSuccessContainer else NaturalPrimaryContainer
                ) {
                    Text(
                        text = if (isFullyGranted) "READY" else "ACTION REQ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFullyGranted) NaturalSuccess else NaturalPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = "Allow the app to modify secure system properties, screen controls, and window animation settings.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = NaturalOnSurfaceVariant,
                    lineHeight = 18.sp
                )
            )

            // Permission badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PermissionBadge(
                    label = "WRITE_SETTINGS",
                    granted = status.canWriteSystem,
                    modifier = Modifier.weight(1f)
                )
                PermissionBadge(
                    label = "WRITE_SECURE_SETTINGS",
                    granted = status.hasSecureSettingsPermission,
                    modifier = Modifier.weight(1f)
                )
            }

            if (!isFullyGranted) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onGrantViaShizuku,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("grant_via_shizuku_button"),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NaturalPrimary,
                            contentColor = NaturalOnPrimary
                        ),
                        enabled = shizukuState.hasPermission
                    ) {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (shizukuState.hasPermission) "Grant Permission via Shizuku (1-Tap)" else "Grant via Shizuku (Auth Required)",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedButton(
                        onClick = onOpenAndroidSettings,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_system_settings_perm_button"),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NaturalPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NaturalOutline)
                    ) {
                        Icon(imageVector = Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Open Android Settings to Allow Manually", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionBadge(
    label: String,
    granted: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (granted) NaturalSuccessContainer.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (granted) NaturalSuccess.copy(alpha = 0.3f) else NaturalOutlineVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = if (granted) NaturalSuccess else NaturalOnSurfaceVariant
                )
            )
            Icon(
                imageVector = if (granted) Icons.Default.Check else Icons.Default.Warning,
                contentDescription = null,
                tint = if (granted) NaturalSuccess else NaturalWarning,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnimationScalesCard(
    currentScale: Float,
    onApplyScale: (Float) -> Unit
) {
    val presets = listOf(0.0f, 0.5f, 1.0f, 1.5f, 2.0f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("animation_scales_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = NaturalSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, NaturalOutline)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(NaturalPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Animation, contentDescription = null, tint = NaturalPrimary, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(
                        text = "Window & Animator Scale",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, color = NaturalOnBackground)
                    )
                    Text(
                        text = "Instant transitions or fluid pacing",
                        style = MaterialTheme.typography.bodySmall.copy(color = NaturalOnSurfaceVariant)
                    )
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { scale ->
                    val isSelected = kotlin.math.abs(currentScale - scale) < 0.05f
                    FilterChip(
                        selected = isSelected,
                        onClick = { onApplyScale(scale) },
                        shape = RoundedCornerShape(20.dp),
                        label = {
                            Text(
                                text = when (scale) {
                                    0.0f -> "Off (0x)"
                                    0.5f -> "Fast (0.5x)"
                                    1.0f -> "Normal (1.0x)"
                                    else -> "${scale}x"
                                },
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NaturalPrimary,
                            selectedLabelColor = NaturalOnPrimary,
                            containerColor = NaturalSecondaryContainer,
                            labelColor = NaturalOnBackground
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = NaturalOutlineVariant,
                            selectedBorderColor = NaturalPrimary,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScreenTimeoutCard(
    currentTimeoutMs: Int,
    onApplyTimeout: (Int) -> Unit
) {
    val timeouts = listOf(
        Pair(15000, "15s"),
        Pair(30000, "30s"),
        Pair(60000, "1m"),
        Pair(120000, "2m"),
        Pair(300000, "5m"),
        Pair(600000, "10m"),
        Pair(1800000, "30m")
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("screen_timeout_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = NaturalSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, NaturalOutline)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(NaturalPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.HourglassTop, contentDescription = null, tint = NaturalPrimary, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(
                        text = "Screen Off Timeout",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, color = NaturalOnBackground)
                    )
                    Text(
                        text = "Current: ${currentTimeoutMs / 1000}s",
                        style = MaterialTheme.typography.bodySmall.copy(color = NaturalOnSurfaceVariant)
                    )
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                timeouts.forEach { (ms, label) ->
                    val isSelected = currentTimeoutMs == ms
                    FilterChip(
                        selected = isSelected,
                        onClick = { onApplyTimeout(ms) },
                        shape = RoundedCornerShape(20.dp),
                        label = { Text(text = label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NaturalPrimary,
                            selectedLabelColor = NaturalOnPrimary,
                            containerColor = NaturalSecondaryContainer,
                            labelColor = NaturalOnBackground
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = NaturalOutlineVariant,
                            selectedBorderColor = NaturalPrimary,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun DisplayAndHapticsCard(
    peakRefreshRate: Float,
    hapticEnabled: Boolean,
    onSetRefreshRate: (Float) -> Unit,
    onToggleHaptic: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("display_haptics_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = NaturalSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, NaturalOutline)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Refresh rate
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(NaturalPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = NaturalPrimary, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(text = "Peak Refresh Rate Target", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = NaturalOnBackground))
                        Text(text = "${peakRefreshRate.toInt()} Hz dynamic target", style = MaterialTheme.typography.bodySmall.copy(color = NaturalOnSurfaceVariant))
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(60f, 90f, 120f).forEach { hz ->
                        val isSelected = peakRefreshRate.toInt() == hz.toInt()
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSetRefreshRate(hz) },
                            shape = RoundedCornerShape(16.dp),
                            label = { Text("${hz.toInt()}Hz", fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NaturalPrimary,
                                selectedLabelColor = NaturalOnPrimary,
                                containerColor = NaturalSecondaryContainer,
                                labelColor = NaturalOnBackground
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = NaturalOutlineVariant,
                                selectedBorderColor = NaturalPrimary,
                                enabled = true,
                                selected = isSelected
                            )
                        )
                    }
                }
            }

            // Haptics switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(NaturalPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Vibration, contentDescription = null, tint = NaturalPrimary, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(text = "Haptic Feedback", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = NaturalOnBackground))
                        Text(text = "Vibration on touch & gestures", style = MaterialTheme.typography.bodySmall.copy(color = NaturalOnSurfaceVariant))
                    }
                }
                Switch(
                    checked = hapticEnabled,
                    onCheckedChange = onToggleHaptic,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = NaturalPrimary,
                        uncheckedThumbColor = NaturalOnSurfaceVariant,
                        uncheckedTrackColor = NaturalOutlineVariant
                    )
                )
            }
        }
    }
}

@Composable
fun CustomSettingEditorCard(
    selectedNamespace: SettingNamespace,
    onNamespaceSelected: (SettingNamespace) -> Unit,
    key: String,
    onKeyChange: (String) -> Unit,
    value: String,
    onValueChange: (String) -> Unit,
    onWrite: () -> Unit,
    onRead: () -> Unit,
    readResult: String?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("custom_setting_editor_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = NaturalSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, NaturalOutline)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(NaturalPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = NaturalPrimary, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(
                        text = "Custom Setting Read / Write",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, color = NaturalOnBackground)
                    )
                    Text(
                        text = "Directly modify any system, secure, or global key",
                        style = MaterialTheme.typography.bodySmall.copy(color = NaturalOnSurfaceVariant)
                    )
                }
            }

            // Namespace selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SettingNamespace.values().forEach { ns ->
                    val isSelected = selectedNamespace == ns
                    FilterChip(
                        selected = isSelected,
                        onClick = { onNamespaceSelected(ns) },
                        shape = RoundedCornerShape(16.dp),
                        label = { Text(ns.name, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NaturalPrimary,
                            selectedLabelColor = NaturalOnPrimary,
                            containerColor = NaturalSecondaryContainer,
                            labelColor = NaturalOnBackground
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = NaturalOutlineVariant,
                            selectedBorderColor = NaturalPrimary,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            // Key Input
            OutlinedTextField(
                value = key,
                onValueChange = onKeyChange,
                label = { Text("Setting Key (e.g. animator_duration_scale)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_setting_key_input"),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NaturalPrimary,
                    unfocusedBorderColor = NaturalOutline,
                    focusedLabelColor = NaturalPrimary,
                    unfocusedLabelColor = NaturalOnSurfaceVariant
                )
            )

            // Value Input
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text("Setting Value (e.g. 0.5, 1, off)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_setting_val_input"),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NaturalPrimary,
                    unfocusedBorderColor = NaturalOutline,
                    focusedLabelColor = NaturalPrimary,
                    unfocusedLabelColor = NaturalOnSurfaceVariant
                )
            )

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRead,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("custom_setting_read_button"),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NaturalPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NaturalOutline)
                ) {
                    Text("Read Key", fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = onWrite,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("custom_setting_write_button"),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NaturalPrimary,
                        contentColor = NaturalOnPrimary
                    )
                ) {
                    Text("Write Key", fontWeight = FontWeight.SemiBold)
                }
            }

            if (readResult != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = NaturalSecondaryContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NaturalOutlineVariant)
                ) {
                    Text(
                        text = "Result: $readResult",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = NaturalPrimary,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}
