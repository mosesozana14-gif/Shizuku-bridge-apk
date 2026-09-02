package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import com.example.settings.SystemSettingsStatus
import com.example.shizuku.ShizukuState
import com.example.ui.MainViewModel
import com.example.ui.theme.NaturalBackground
import com.example.ui.theme.NaturalDanger
import com.example.ui.theme.NaturalDangerContainer
import com.example.ui.theme.NaturalOnBackground
import com.example.ui.theme.NaturalOnPrimary
import com.example.ui.theme.NaturalOnSurfaceVariant
import com.example.ui.theme.NaturalOutline
import com.example.ui.theme.NaturalOutlineVariant
import com.example.ui.theme.NaturalPrimary
import com.example.ui.theme.NaturalPrimaryContainer
import com.example.ui.theme.NaturalSecondaryContainer
import com.example.ui.theme.NaturalSuccess
import com.example.ui.theme.NaturalSuccessContainer
import com.example.ui.theme.NaturalSurface

@Composable
fun ShizukuDashboardTab(
    viewModel: MainViewModel,
    shizukuState: ShizukuState,
    settingsStatus: SystemSettingsStatus,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showGuide by remember { mutableStateOf(!shizukuState.isRunning) }

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
                onRefresh = { viewModel.refreshAll() },
                onOpenShizuku = { viewModel.launchShizuku() },
                onTestPrivilege = { viewModel.testShizukuPrivilege() }
            )
        }

        // Elevation & System Settings Permissions
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

        // Shizuku Service Diagnostics
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shizuku_diagnostics_card"),
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
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = NaturalPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Service Diagnostics",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = NaturalOnBackground
                                )
                            )
                            Text(
                                text = "IPC connection and process verification",
                                style = MaterialTheme.typography.bodySmall.copy(color = NaturalOnSurfaceVariant)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DiagnosticRow(
                            label = "Binder Connection",
                            status = if (shizukuState.isRunning) "Connected (Alive)" else "Disconnected",
                            isOk = shizukuState.isRunning
                        )
                        DiagnosticRow(
                            label = "API Version",
                            status = if (shizukuState.version > 0) "v${shizukuState.version} (Supported)" else "N/A",
                            isOk = shizukuState.version >= 11
                        )
                        DiagnosticRow(
                            label = "Execution UID",
                            status = if (shizukuState.uid == 0) "0 (Root)" else if (shizukuState.uid == 2000) "2000 (Shell / ADB)" else if (shizukuState.uid > 0) "${shizukuState.uid}" else "Unbound",
                            isOk = shizukuState.uid >= 0
                        )
                        DiagnosticRow(
                            label = "App Authorization",
                            status = if (shizukuState.hasPermission) "Authorized" else "Pending Authorization",
                            isOk = shizukuState.hasPermission
                        )
                    }
                }
            }
        }

        // Shizuku Start Guide
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shizuku_guide_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = NaturalSecondaryContainer),
                border = androidx.compose.foundation.BorderStroke(1.dp, NaturalOutlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showGuide = !showGuide },
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
                                    .background(Color.White.copy(alpha = 0.65f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Wifi,
                                    contentDescription = null,
                                    tint = NaturalPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Wireless Debugging & ADB Guide",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = NaturalOnBackground
                                    )
                                )
                                Text(
                                    text = "How to start Shizuku without root",
                                    style = MaterialTheme.typography.bodySmall.copy(color = NaturalOnSurfaceVariant)
                                )
                            }
                        }
                        Text(
                            text = if (showGuide) "Hide" else "Show",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = NaturalPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    AnimatedVisibility(visible = showGuide) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            GuideStep(
                                number = "1",
                                title = "Enable Developer Options & Wireless Debugging",
                                detail = "Go to Android Settings > About Phone > Tap 'Build Number' 7 times. In Developer Options, enable 'Wireless Debugging' (must be on Wi-Fi or Hotspot)."
                            )
                            GuideStep(
                                number = "2",
                                title = "Pair Directly on Phone",
                                detail = "In Shizuku app, tap 'Pairing' under Wireless Debugging. Tap 'Developer options', choose 'Pair device with pairing code'. Enter the 6-digit code in the notification."
                            )
                            GuideStep(
                                number = "3",
                                title = "Start Shizuku on Mobile",
                                detail = "Return to Shizuku app and tap 'Start'. Shizuku will start running in the background."
                            )
                            GuideStep(
                                number = "4",
                                title = "Authorize This App",
                                detail = "In Shizuku app under 'Authorized Applications', toggle Moses Mod ON, or tap 'Authorize Shizuku Permission' right here!"
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun DiagnosticRow(
    label: String,
    status: String,
    isOk: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                color = NaturalOnBackground
            )
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isOk) NaturalSuccess else NaturalDanger)
            )
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isOk) NaturalSuccess else NaturalOnSurfaceVariant,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
private fun GuideStep(
    number: String,
    title: String,
    detail: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.6f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(NaturalPrimary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = NaturalOnPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = NaturalOnBackground
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = NaturalOnSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            )
        }
    }
}
