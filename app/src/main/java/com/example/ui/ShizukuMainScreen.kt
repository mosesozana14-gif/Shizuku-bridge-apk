package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.BitLifeStatsEditorTab
import com.example.ui.components.ConsoleTab
import com.example.ui.components.HeaderBar
import com.example.ui.components.ShizukuDashboardTab
import com.example.ui.components.SystemSettingsTab
import com.example.ui.theme.NaturalBackground
import com.example.ui.theme.NaturalOnSurfaceVariant
import com.example.ui.theme.NaturalPrimary
import com.example.ui.theme.NaturalPrimaryContainer
import com.example.ui.theme.NaturalSecondaryContainer

enum class MainTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    DASHBOARD("Status", Icons.Default.VerifiedUser),
    BITLIFE("BitLife", Icons.Default.AutoAwesome),
    SYSTEM_SETTINGS("Tweaks", Icons.Default.Tune),
    CONSOLE("ADB Shell", Icons.Default.Terminal)
}

@Composable
fun ShizukuMainScreen(viewModel: MainViewModel) {
    var selectedTab by remember { mutableStateOf(MainTab.BITLIFE) }

    val shizukuState by viewModel.shizukuState.collectAsStateWithLifecycle()
    val settingsStatus by viewModel.settingsStatus.collectAsStateWithLifecycle()
    val bitLifeScanResult by viewModel.bitLifeScanResult.collectAsStateWithLifecycle()
    val bitLifeStats by viewModel.bitLifeStats.collectAsStateWithLifecycle()
    val bitLifeSelectedSlot by viewModel.bitLifeSelectedSlot.collectAsStateWithLifecycle()
    val consoleLogs by viewModel.consoleLogs.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = NaturalBackground,
        topBar = {
            HeaderBar(
                shizukuState = shizukuState,
                isBusy = isBusy,
                onRefresh = { viewModel.refreshAll() }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
                color = NaturalSecondaryContainer
            ) {
                NavigationBar(
                    containerColor = NaturalSecondaryContainer,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("main_navigation_bar"),
                    tonalElevation = 0.dp
                ) {
                    MainTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    tint = if (isSelected) NaturalPrimary else NaturalOnSurfaceVariant
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp
                                    )
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NaturalPrimary,
                                selectedTextColor = NaturalPrimary,
                                indicatorColor = NaturalPrimaryContainer,
                                unselectedIconColor = NaturalOnSurfaceVariant,
                                unselectedTextColor = NaturalOnSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NaturalBackground)
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                MainTab.DASHBOARD -> {
                    ShizukuDashboardTab(
                        viewModel = viewModel,
                        shizukuState = shizukuState,
                        settingsStatus = settingsStatus
                    )
                }
                MainTab.BITLIFE -> {
                    BitLifeStatsEditorTab(
                        viewModel = viewModel,
                        shizukuState = shizukuState,
                        scanResult = bitLifeScanResult,
                        currentStats = bitLifeStats,
                        selectedSlot = bitLifeSelectedSlot,
                        isBusy = isBusy
                    )
                }
                MainTab.SYSTEM_SETTINGS -> {
                    SystemSettingsTab(
                        viewModel = viewModel,
                        shizukuState = shizukuState,
                        settingsStatus = settingsStatus
                    )
                }
                MainTab.CONSOLE -> {
                    ConsoleTab(
                        viewModel = viewModel,
                        logs = consoleLogs,
                        isBusy = isBusy
                    )
                }
            }
        }
    }
}
