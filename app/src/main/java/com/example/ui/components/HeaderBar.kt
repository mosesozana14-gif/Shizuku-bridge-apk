package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shizuku.ShizukuState
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
import com.example.ui.theme.NaturalSecondaryContainer
import com.example.ui.theme.NaturalSuccess
import com.example.ui.theme.NaturalSuccessContainer
import com.example.ui.theme.NaturalSurface
import com.example.ui.theme.NaturalWarning
import com.example.ui.theme.NaturalWarningContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderBar(
    shizukuState: ShizukuState,
    isBusy: Boolean,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(NaturalBackground)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
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
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(NaturalPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "App Icon",
                        tint = NaturalPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Shizuku Bridge",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = 24.sp,
                            color = NaturalOnBackground
                        )
                    )
                    Text(
                        text = "Privileged Service & System Tweaker",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NaturalOnSurfaceVariant,
                            fontSize = 13.sp
                        )
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Status pill badge
                val (badgeBg, badgeText, badgeColor) = when {
                    shizukuState.isRunning && shizukuState.hasPermission -> Triple(
                        NaturalSuccessContainer,
                        "ACTIVE",
                        NaturalSuccess
                    )
                    shizukuState.isRunning -> Triple(
                        NaturalPrimaryContainer,
                        "AUTH REQ",
                        NaturalPrimary
                    )
                    else -> Triple(
                        NaturalDangerContainer,
                        "OFFLINE",
                        NaturalDanger
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = badgeBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f)),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(badgeColor)
                        )
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(NaturalSecondaryContainer)
                        .testTag("refresh_button")
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = NaturalPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Status",
                            tint = NaturalPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShizukuStatusCard(
    state: ShizukuState,
    onRequestPermission: () -> Unit,
    onRefresh: () -> Unit,
    onOpenShizuku: () -> Unit = {},
    onTestPrivilege: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isReady = state.isRunning && state.hasPermission

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("shizuku_status_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isReady) NaturalPrimaryContainer else NaturalSecondaryContainer
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isReady) NaturalOutline.copy(alpha = 0.4f) else NaturalOutlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.65f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isReady) Icons.Default.VerifiedUser
                        else if (state.isRunning) Icons.Default.Key
                        else Icons.Default.PowerSettingsNew,
                        contentDescription = "Shizuku Status",
                        tint = if (isReady) NaturalPrimary
                        else if (state.isRunning) NaturalWarning
                        else NaturalDanger,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isReady) "Shizuku is Connected & Ready"
                        else if (state.isRunning) "Shizuku Service Detected"
                        else "Shizuku is Offline",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = NaturalOnPrimaryContainer
                        )
                    )
                    Text(
                        text = when {
                            state.isRunning && state.hasPermission -> "Authorized via Wireless Debugging (UID: ${state.uid})"
                            state.isRunning -> "Service running! Tap 'Authorize' to link this app."
                            else -> "Start Shizuku via Wireless Debugging on mobile"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NaturalPrimary.copy(alpha = 0.85f),
                            fontSize = 13.sp
                        )
                    )
                }
            }

            // Specs Row in clean translucent card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.55f))
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatusMetric(label = "STATUS", value = if (state.isRunning) "RUNNING" else "STOPPED")
                StatusMetric(label = "VERSION", value = if (state.version > 0) "v${state.version}" else "N/A")
                StatusMetric(label = "UID", value = if (state.uid >= 0) "${state.uid}" else "N/A")
                StatusMetric(
                    label = "PERMISSION",
                    value = if (state.hasPermission) "GRANTED" else "PENDING",
                    valueColor = if (state.hasPermission) NaturalSuccess else NaturalPrimary
                )
            }

            if (isReady) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onTestPrivilege,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("test_shizuku_privilege_button"),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NaturalPrimary,
                            contentColor = NaturalOnPrimary
                        )
                    ) {
                        Text(text = "⚡ Test Privileged Shell", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    OutlinedButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .testTag("refresh_shizuku_card_button"),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(16.dp))
                    }
                }
            } else if (state.isRunning && !state.hasPermission) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onRequestPermission,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("request_shizuku_perm_button"),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NaturalPrimary,
                            contentColor = NaturalOnPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Authorize Shizuku Permission", fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = onOpenShizuku,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(text = "Open Shizuku App", fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenShizuku,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_shizuku_app_button"),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NaturalPrimary,
                                contentColor = NaturalOnPrimary
                            )
                        ) {
                            Text(text = "📱 Open Shizuku App", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                        Button(
                            onClick = onRefresh,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("recheck_shizuku_button"),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NaturalSecondaryContainer,
                                contentColor = NaturalPrimary
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Re-check", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusMetric(
    label: String,
    value: String,
    valueColor: Color = NaturalOnBackground
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = NaturalOnSurfaceVariant,
                letterSpacing = 0.5.sp,
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = valueColor
            )
        )
    }
}
