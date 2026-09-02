package com.example.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bitlife.BitLifeSavePatcher
import com.example.bitlife.BitLifeSaveScanResult
import com.example.bitlife.BitLifeSlotInfo
import com.example.bitlife.BitLifeStats
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
import com.example.ui.theme.NaturalSecondary
import com.example.ui.theme.NaturalSecondaryContainer
import com.example.ui.theme.NaturalSuccess
import com.example.ui.theme.NaturalSuccessContainer
import com.example.ui.theme.NaturalSurface

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BitLifeStatsEditorTab(
    viewModel: MainViewModel,
    shizukuState: ShizukuState,
    scanResult: BitLifeSaveScanResult,
    currentStats: BitLifeStats,
    selectedSlot: BitLifeSlotInfo?,
    selectedGame: com.example.bitlife.SupportedGame,
    isBusy: Boolean,
    modifier: Modifier = Modifier
) {
    val isOverlayActive by viewModel.isOverlayActive.collectAsStateWithLifecycle()

    var editableStats by remember(currentStats) { mutableStateOf(currentStats) }
    var moneyInput by remember(editableStats.bankBalance) { mutableStateOf(editableStats.bankBalance.toString()) }
    var salaryInput by remember(editableStats.salary) { mutableStateOf(editableStats.salary.toString()) }
    var ageInput by remember(editableStats.age) { mutableStateOf(editableStats.age.toString()) }
    var ageAtDeathInput by remember(editableStats.ageAtDeath) { mutableStateOf(editableStats.ageAtDeath.toString()) }

    // Social inputs
    var ytInput by remember(editableStats.socialStats.youtubeSubscribers) { mutableStateOf(editableStats.socialStats.youtubeSubscribers.toString()) }
    var tiktokInput by remember(editableStats.socialStats.tiktokFollowers) { mutableStateOf(editableStats.socialStats.tiktokFollowers.toString()) }
    var igInput by remember(editableStats.socialStats.instagramFollowers) { mutableStateOf(editableStats.socialStats.instagramFollowers.toString()) }
    var xInput by remember(editableStats.socialStats.twitterFollowers) { mutableStateOf(editableStats.socialStats.twitterFollowers.toString()) }
    var twitchInput by remember(editableStats.socialStats.twitchFollowers) { mutableStateOf(editableStats.socialStats.twitchFollowers.toString()) }

    var showAllPerksDialog by remember { mutableStateOf(false) }

    val openSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.loadPickedFile(uri)
        }
    }

    val exportSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            val finalStats = editableStats.copy(
                bankBalance = moneyInput.toLongOrNull() ?: editableStats.bankBalance,
                salary = salaryInput.toLongOrNull() ?: editableStats.salary,
                age = ageInput.toIntOrNull() ?: editableStats.age,
                ageAtDeath = ageAtDeathInput.toIntOrNull() ?: editableStats.ageAtDeath,
                socialStats = editableStats.socialStats.copy(
                    youtubeSubscribers = ytInput.toLongOrNull() ?: editableStats.socialStats.youtubeSubscribers,
                    tiktokFollowers = tiktokInput.toLongOrNull() ?: editableStats.socialStats.tiktokFollowers,
                    instagramFollowers = igInput.toLongOrNull() ?: editableStats.socialStats.instagramFollowers,
                    twitterFollowers = xInput.toLongOrNull() ?: editableStats.socialStats.twitterFollowers,
                    twitchFollowers = twitchInput.toLongOrNull() ?: editableStats.socialStats.twitchFollowers
                )
            )
            viewModel.exportPatchedSave(uri, finalStats)
        }
    }

    val exportMonetizationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            viewModel.exportGodModeMonetization(uri)
        }
    }

    LaunchedEffect(Unit) {
        if (shizukuState.hasPermission) {
            viewModel.scanBitLife()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NaturalBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(2.dp))
            // 1. MOSES MOD MENU FLOATING OVERLAY LAUNCHER BANNER
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("moses_overlay_launcher_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = NaturalPrimaryContainer),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, NaturalPrimary.copy(alpha = 0.5f))
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
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(NaturalPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = NaturalOnPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "⚡ MOSES MOD MENU",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NaturalPrimary,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                                Text(
                                    text = "Moses is the GOAT • Floating Overlay HUD",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = NaturalOnBackground.copy(alpha = 0.8f),
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }

                        Switch(
                            checked = isOverlayActive,
                            onCheckedChange = { viewModel.toggleOverlay() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NaturalOnPrimary,
                                checkedTrackColor = NaturalPrimary,
                                uncheckedThumbColor = NaturalOutline,
                                uncheckedTrackColor = NaturalSecondaryContainer
                            ),
                            modifier = Modifier.testTag("toggle_floating_overlay_switch")
                        )
                    }

                    Text(
                        text = if (isOverlayActive)
                            "✓ Floating overlay active! A draggable '⚡ MOSES MOD MENU' badge is now floating over BitLife on your screen. Tap it anytime to open the full mod HUD."
                        else
                            "Enable the floating overlay to customize social media subscribers, followers, and instant power-ups directly while playing BitLife.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NaturalOnSurfaceVariant,
                            lineHeight = 17.sp
                        )
                    )

                    if (!viewModel.hasOverlayPermission()) {
                        OutlinedButton(
                            onClick = { viewModel.requestOverlayPermission() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NaturalPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Grant 'Draw Over Other Apps' Permission", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Top Slot & Status Header
        item {
            BitLifeHeaderCard(
                scanResult = scanResult,
                selectedSlot = selectedSlot,
                selectedGame = selectedGame,
                isBusy = isBusy,
                hasShizukuPermission = shizukuState.hasPermission,
                onAutoFind = { viewModel.autoFindBitLife() },
                onAutoInjectGodLife = { viewModel.autoInjectGodLife() },
                onLaunchBitLife = { viewModel.launchBitLife() },
                onRequestShizuku = { viewModel.requestShizukuPermission() },
                onScan = { viewModel.scanBitLife() },
                onSelectGame = { game -> viewModel.selectGame(game) },
                onSelectSlot = { slot ->
                    viewModel.selectBitLifeSlot(slot)
                },
                onPickFile = { openSaveLauncher.launch(arrayOf("*/*")) },
                onLaunchZArchiver = { viewModel.launchZArchiver() },
                isZArchiverInstalled = viewModel.isZArchiverInstalled()
            )
        }

        // Quick Preset Macros
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bitlife_presets_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = NaturalSecondaryContainer),
                border = androidx.compose.foundation.BorderStroke(1.dp, NaturalOutlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "QUICK STATS & MOD PRESETS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NaturalPrimary,
                            letterSpacing = 0.5.sp
                        )
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetChip(
                            label = "🌟 Max All Core (100%)",
                            onClick = {
                                editableStats = editableStats.copy(
                                    happiness = 100,
                                    health = 100,
                                    smarts = 100,
                                    looks = 100,
                                    jobPerformance = 100,
                                    schoolGrades = 100
                                )
                            }
                        )
                        PresetChip(
                            label = "👑 Max All Hidden Talents",
                            onClick = {
                                editableStats = editableStats.copy(
                                    karma = 100,
                                    fame = 100,
                                    athleticism = 100,
                                    discipline = 100,
                                    willpower = 100,
                                    musicTalent = 100,
                                    actingTalent = 100,
                                    voiceTalent = 100,
                                    streetSmarts = 100,
                                    fertility = 100,
                                    generosity = 100
                                )
                            }
                        )
                        PresetChip(
                            label = "📱 Max 100M All Socials",
                            onClick = {
                                val s = editableStats.socialStats.copy(
                                    youtubeSubscribers = 100_000_000L,
                                    tiktokFollowers = 100_000_000L,
                                    instagramFollowers = 100_000_000L,
                                    twitterFollowers = 50_000_000L,
                                    twitchFollowers = 25_000_000L,
                                    isVerified = true,
                                    viralBoost = true
                                )
                                editableStats = editableStats.copy(socialStats = s)
                                ytInput = "100000000"
                                tiktokInput = "100000000"
                                igInput = "100000000"
                                xInput = "50000000"
                                twitchInput = "25000000"
                            }
                        )
                        PresetChip(
                            label = "⚡ Turn On All Godly Power-Ups",
                            onClick = {
                                val p = editableStats.powerUps.copy(
                                    lotteryAutoWin = true,
                                    casino100Win = true,
                                    crime100Success = true,
                                    prisonEscape100 = true,
                                    diseaseImmunity = true,
                                    plasticSurgeryFlawless = true,
                                    instantPromotionCEO = true,
                                    heirloomsUnlocked = true,
                                    fertilityTwinsTriplets = true,
                                    unlimitedTimeMachine = true
                                )
                                editableStats = editableStats.copy(powerUps = p)
                            }
                        )
                        PresetChip(
                            label = "💎 Set $100 Million",
                            onClick = {
                                editableStats = editableStats.copy(bankBalance = 100_000_000L)
                                moneyInput = "100000000"
                            }
                        )
                        PresetChip(
                            label = "⏳ Immortality (Age 1000)",
                            onClick = {
                                editableStats = editableStats.copy(ageAtDeath = 1000)
                                ageAtDeathInput = "1000"
                            }
                        )
                    }
                }
            }
        }

        // Primary Save & Apply Action Button + Export Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val finalStats = editableStats.copy(
                            bankBalance = moneyInput.toLongOrNull() ?: editableStats.bankBalance,
                            salary = salaryInput.toLongOrNull() ?: editableStats.salary,
                            age = ageInput.toIntOrNull() ?: editableStats.age,
                            ageAtDeath = ageAtDeathInput.toIntOrNull() ?: editableStats.ageAtDeath,
                            socialStats = editableStats.socialStats.copy(
                                youtubeSubscribers = ytInput.toLongOrNull() ?: editableStats.socialStats.youtubeSubscribers,
                                tiktokFollowers = tiktokInput.toLongOrNull() ?: editableStats.socialStats.tiktokFollowers,
                                instagramFollowers = igInput.toLongOrNull() ?: editableStats.socialStats.instagramFollowers,
                                twitterFollowers = xInput.toLongOrNull() ?: editableStats.socialStats.twitterFollowers,
                                twitchFollowers = twitchInput.toLongOrNull() ?: editableStats.socialStats.twitchFollowers
                            )
                        )
                        viewModel.saveBitLifeStats(finalStats)
                    },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(58.dp)
                        .testTag("save_bitlife_stats_button"),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NaturalPrimary,
                        contentColor = NaturalOnPrimary
                    ),
                    enabled = !isBusy && selectedSlot != null
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = NaturalOnPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saving...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (selectedSlot?.isPickedFile == true) "SAVE TO PICKED FILE" else "SAVE & APPLY TO BITLIFE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                OutlinedButton(
                    onClick = { exportSaveLauncher.launch("savedLife.data") },
                    modifier = Modifier
                        .weight(0.85f)
                        .height(58.dp)
                        .testTag("export_save_button"),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NaturalPrimary),
                    enabled = !isBusy
                ) {
                    Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("EXPORT FILE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // 2. SOCIAL MEDIA & SUBSCRIBERS HUB
        item {
            StatCategoryCard(
                title = "Social Media Followers & Subscribers",
                icon = Icons.Default.Share,
                description = "YouTube subscribers, TikTok, Instagram & verification badges"
            ) {
                // YouTube
                SocialInputRow(
                    platform = "YouTube Subscribers",
                    value = ytInput,
                    onValueChange = {
                        ytInput = it
                        it.toLongOrNull()?.let { num ->
                            editableStats = editableStats.copy(
                                socialStats = editableStats.socialStats.copy(youtubeSubscribers = num)
                            )
                        }
                    },
                    presets = listOf("+1M" to 1_000_000L, "+10M" to 10_000_000L, "+50M" to 50_000_000L, "100M" to 100_000_000L)
                )

                // TikTok
                SocialInputRow(
                    platform = "TikTok Followers",
                    value = tiktokInput,
                    onValueChange = {
                        tiktokInput = it
                        it.toLongOrNull()?.let { num ->
                            editableStats = editableStats.copy(
                                socialStats = editableStats.socialStats.copy(tiktokFollowers = num)
                            )
                        }
                    },
                    presets = listOf("+500K" to 500_000L, "+5M" to 5_000_000L, "+20M" to 20_000_000L, "50M" to 50_000_000L)
                )

                // Instagram
                SocialInputRow(
                    platform = "Instagram Followers",
                    value = igInput,
                    onValueChange = {
                        igInput = it
                        it.toLongOrNull()?.let { num ->
                            editableStats = editableStats.copy(
                                socialStats = editableStats.socialStats.copy(instagramFollowers = num)
                            )
                        }
                    },
                    presets = listOf("+1M" to 1_000_000L, "+10M" to 10_000_000L, "+50M" to 50_000_000L, "100M" to 100_000_000L)
                )

                // Twitter / X
                SocialInputRow(
                    platform = "Twitter / X Followers",
                    value = xInput,
                    onValueChange = {
                        xInput = it
                        it.toLongOrNull()?.let { num ->
                            editableStats = editableStats.copy(
                                socialStats = editableStats.socialStats.copy(twitterFollowers = num)
                            )
                        }
                    },
                    presets = listOf("+500K" to 500_000L, "+2M" to 2_000_000L, "+10M" to 10_000_000L, "25M" to 25_000_000L)
                )

                // Twitch
                SocialInputRow(
                    platform = "Twitch Followers",
                    value = twitchInput,
                    onValueChange = {
                        twitchInput = it
                        it.toLongOrNull()?.let { num ->
                            editableStats = editableStats.copy(
                                socialStats = editableStats.socialStats.copy(twitchFollowers = num)
                            )
                        }
                    },
                    presets = listOf("+250K" to 250_000L, "+1M" to 1_000_000L, "+5M" to 5_000_000L, "10M" to 10_000_000L)
                )

                // Toggles for Verified & Viral
                ToggleItemRow(
                    label = "Verified Checkmark on All Platforms",
                    checked = editableStats.socialStats.isVerified,
                    onCheckedChange = {
                        editableStats = editableStats.copy(
                            socialStats = editableStats.socialStats.copy(isVerified = it)
                        )
                    }
                )

                ToggleItemRow(
                    label = "100% Viral Boost Multiplier",
                    checked = editableStats.socialStats.viralBoost,
                    onCheckedChange = {
                        editableStats = editableStats.copy(
                            socialStats = editableStats.socialStats.copy(viralBoost = it)
                        )
                    }
                )
            }
        }

        // 3. GODLY POWER-UPS & PERKS HUB
        item {
            StatCategoryCard(
                title = "Godly Power-Ups & Cheats",
                icon = Icons.Default.Bolt,
                description = "Lottery jackpot auto-win, casino 100%, crime immunity & prison escape"
            ) {
                ToggleItemRow(
                    label = "🎰 Lottery Auto-Win 100% (Instant Jackpot)",
                    checked = editableStats.powerUps.lotteryAutoWin,
                    onCheckedChange = {
                        editableStats = editableStats.copy(
                            powerUps = editableStats.powerUps.copy(lotteryAutoWin = it)
                        )
                    }
                )

                ToggleItemRow(
                    label = "🃏 Casino & Blackjack 100% Win Rate",
                    checked = editableStats.powerUps.casino100Win,
                    onCheckedChange = {
                        editableStats = editableStats.copy(
                            powerUps = editableStats.powerUps.copy(casino100Win = it)
                        )
                    }
                )

                ToggleItemRow(
                    label = "🗡️ 100% Crime/Murder Success & 0% Arrest (Assassin Mode)",
                    checked = editableStats.powerUps.crime100Success,
                    onCheckedChange = {
                        editableStats = editableStats.copy(
                            powerUps = editableStats.powerUps.copy(crime100Success = it)
                        )
                    }
                )

                ToggleItemRow(
                    label = "🏃 100% Prison Escape (Ghost Inmate)",
                    checked = editableStats.powerUps.prisonEscape100,
                    onCheckedChange = {
                        editableStats = editableStats.copy(
                            powerUps = editableStats.powerUps.copy(prisonEscape100 = it)
                        )
                    }
                )

                ToggleItemRow(
                    label = "💉 Disease & Illness Complete Immunity (Instant Cure)",
                    checked = editableStats.powerUps.diseaseImmunity,
                    onCheckedChange = {
                        editableStats = editableStats.copy(
                            powerUps = editableStats.powerUps.copy(diseaseImmunity = it)
                        )
                    }
                )

                ToggleItemRow(
                    label = "💅 Flawless Plastic Surgery (Zero Botch)",
                    checked = editableStats.powerUps.plasticSurgeryFlawless,
                    onCheckedChange = {
                        editableStats = editableStats.copy(
                            powerUps = editableStats.powerUps.copy(plasticSurgeryFlawless = it)
                        )
                    }
                )

                ToggleItemRow(
                    label = "👔 Instant Promotion to CEO / Godfather",
                    checked = editableStats.powerUps.instantPromotionCEO,
                    onCheckedChange = {
                        editableStats = editableStats.copy(
                            powerUps = editableStats.powerUps.copy(instantPromotionCEO = it)
                        )
                    }
                )

                ToggleItemRow(
                    label = "👶 100% Fertility + Guaranteed Twins/Triplets",
                    checked = editableStats.powerUps.fertilityTwinsTriplets,
                    onCheckedChange = {
                        editableStats = editableStats.copy(
                            powerUps = editableStats.powerUps.copy(fertilityTwinsTriplets = it)
                        )
                    }
                )

                ToggleItemRow(
                    label = "⏳ Unlimited Free Time Machine Jump",
                    checked = editableStats.powerUps.unlimitedTimeMachine,
                    onCheckedChange = {
                        editableStats = editableStats.copy(
                            powerUps = editableStats.powerUps.copy(unlimitedTimeMachine = it)
                        )
                    }
                )
            }
        }

        // 4. Core Life Stats
        item {
            StatCategoryCard(
                title = "Core Life Attributes",
                icon = Icons.Default.Favorite,
                description = "Fundamental attributes shown on the main character bar"
            ) {
                StatSliderRow(
                    label = "Happiness",
                    value = editableStats.happiness,
                    onValueChange = { editableStats = editableStats.copy(happiness = it) }
                )
                StatSliderRow(
                    label = "Health",
                    value = editableStats.health,
                    onValueChange = { editableStats = editableStats.copy(health = it) }
                )
                StatSliderRow(
                    label = "Smarts (Intelligence)",
                    value = editableStats.smarts,
                    onValueChange = { editableStats = editableStats.copy(smarts = it) }
                )
                StatSliderRow(
                    label = "Looks (Appearance)",
                    value = editableStats.looks,
                    onValueChange = { editableStats = editableStats.copy(looks = it) }
                )
            }
        }

        // 5. Finances & Career
        item {
            StatCategoryCard(
                title = "Finances & Career",
                icon = Icons.Default.AccountBalance,
                description = "Bank balance, annual salary, job performance and school grades"
            ) {
                OutlinedTextField(
                    value = moneyInput,
                    onValueChange = {
                        moneyInput = it
                        it.toLongOrNull()?.let { b -> editableStats = editableStats.copy(bankBalance = b) }
                    },
                    label = { Text("Bank Balance ($)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_bank_balance"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NaturalPrimary,
                        unfocusedBorderColor = NaturalOutline
                    )
                )

                // Quick Add Money Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "+$10M" to 10_000_000L,
                        "+$100M" to 100_000_000L,
                        "+$1B" to 1_000_000_000L,
                        "+$10B" to 10_000_000_000L
                    ).forEach { (label, amount) ->
                        Button(
                            onClick = {
                                val cur = moneyInput.toLongOrNull() ?: 0L
                                val updated = cur + amount
                                moneyInput = updated.toString()
                                editableStats = editableStats.copy(bankBalance = updated)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NaturalSecondaryContainer,
                                contentColor = NaturalPrimary
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 6.dp)
                        ) {
                            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = salaryInput,
                    onValueChange = {
                        salaryInput = it
                        it.toLongOrNull()?.let { s -> editableStats = editableStats.copy(salary = s) }
                    },
                    label = { Text("Annual Salary / Pay ($)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NaturalPrimary,
                        unfocusedBorderColor = NaturalOutline
                    )
                )

                StatSliderRow(
                    label = "Job Performance",
                    value = editableStats.jobPerformance,
                    onValueChange = { editableStats = editableStats.copy(jobPerformance = it) }
                )

                StatSliderRow(
                    label = "School Grades",
                    value = editableStats.schoolGrades,
                    onValueChange = { editableStats = editableStats.copy(schoolGrades = it) }
                )
            }
        }

        // 6. Hidden Talents & Special Attributes
        item {
            StatCategoryCard(
                title = "Talents & Hidden Attributes",
                icon = Icons.Default.AutoAwesome,
                description = "Special gifts and hidden character personality parameters"
            ) {
                StatSliderRow(
                    label = "Karma",
                    value = editableStats.karma,
                    onValueChange = { editableStats = editableStats.copy(karma = it) }
                )
                StatSliderRow(
                    label = "Fame",
                    value = editableStats.fame,
                    onValueChange = { editableStats = editableStats.copy(fame = it) }
                )
                StatSliderRow(
                    label = "Athleticism (Sports)",
                    value = editableStats.athleticism,
                    onValueChange = { editableStats = editableStats.copy(athleticism = it) }
                )
                StatSliderRow(
                    label = "Discipline",
                    value = editableStats.discipline,
                    onValueChange = { editableStats = editableStats.copy(discipline = it) }
                )
                StatSliderRow(
                    label = "Willpower",
                    value = editableStats.willpower,
                    onValueChange = { editableStats = editableStats.copy(willpower = it) }
                )
                StatSliderRow(
                    label = "Music Talent",
                    value = editableStats.musicTalent,
                    onValueChange = { editableStats = editableStats.copy(musicTalent = it) }
                )
                StatSliderRow(
                    label = "Acting Talent",
                    value = editableStats.actingTalent,
                    onValueChange = { editableStats = editableStats.copy(actingTalent = it) }
                )
                StatSliderRow(
                    label = "Voice / Singing Talent",
                    value = editableStats.voiceTalent,
                    onValueChange = { editableStats = editableStats.copy(voiceTalent = it) }
                )
                StatSliderRow(
                    label = "Street Smarts",
                    value = editableStats.streetSmarts,
                    onValueChange = { editableStats = editableStats.copy(streetSmarts = it) }
                )
                StatSliderRow(
                    label = "Fertility",
                    value = editableStats.fertility,
                    onValueChange = { editableStats = editableStats.copy(fertility = it) }
                )
                StatSliderRow(
                    label = "Generosity",
                    value = editableStats.generosity,
                    onValueChange = { editableStats = editableStats.copy(generosity = it) }
                )
                StatSliderRow(
                    label = "Craziness",
                    value = editableStats.craziness,
                    onValueChange = { editableStats = editableStats.copy(craziness = it) }
                )
            }
        }

        // 7. Age & Longevity
        item {
            StatCategoryCard(
                title = "Age & Longevity",
                icon = Icons.Default.HourglassEmpty,
                description = "Character current age and max lifespan at death"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = ageInput,
                        onValueChange = {
                            ageInput = it
                            it.toIntOrNull()?.let { a -> editableStats = editableStats.copy(age = a) }
                        },
                        label = { Text("Current Age") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NaturalPrimary,
                            unfocusedBorderColor = NaturalOutline
                        )
                    )

                    OutlinedTextField(
                        value = ageAtDeathInput,
                        onValueChange = {
                            ageAtDeathInput = it
                            it.toIntOrNull()?.let { d -> editableStats = editableStats.copy(ageAtDeath = d) }
                        },
                        label = { Text("Lifespan (Death Age)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NaturalPrimary,
                            unfocusedBorderColor = NaturalOutline
                        )
                    )
                }
            }
        }

        // 8. Monetization & God Mode Unlocker with All Perks
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("godmode_unlocker_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = NaturalSecondaryContainer),
                border = androidx.compose.foundation.BorderStroke(1.dp, NaturalOutlineVariant)
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
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(NaturalPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = NaturalPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "God Mode & All Expansions Unlocker",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = NaturalOnBackground
                                )
                            )
                            Text(
                                text = "Unlocks 28+ Official BitLife Packs & Perks",
                                style = MaterialTheme.typography.bodySmall.copy(color = NaturalOnSurfaceVariant)
                            )
                        }
                    }

                    Text(
                        text = "Injects Bitizenship, God Mode, Boss Mode, Unlimited Time Machine, Golden Passport, Hollywood Star, Assassin's Blade, Brass Knuckles, Golden Wrench, Golden Diploma, Challenge Vault, Landlord, Cult, Black Market, Zoo, Secret Agent, Model, Dealer, Vampire, Astronaut, Actor, Musician, Pro Athlete, Politician, Business, Mafia, and Street Hustler Packs.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NaturalOnSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    )

                    Button(
                        onClick = { viewModel.unlockBitLifeGodMode() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("unlock_god_mode_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NaturalPrimary,
                            contentColor = NaturalOnPrimary
                        ),
                        enabled = !isBusy
                    ) {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("UNLOCK GOD MODE & ALL 28+ EXPANSIONS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { exportMonetizationLauncher.launch("MonetizationVars") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("export_monetization_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, NaturalPrimary),
                        enabled = !isBusy
                    ) {
                        Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("EXPORT MONETIZATIONVARS (FOR ZARCHIVER)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { showAllPerksDialog = !showAllPerksDialog },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NaturalPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.MilitaryTech, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (showAllPerksDialog) "Hide All Unlocked Perks" else "View All 28+ Unlocked Perks & Items", fontSize = 12.sp)
                    }

                    AnimatedVisibility(visible = showAllPerksDialog) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.5f))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BitLifeSavePatcher.getAllPerksList().forEach { perk ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = perk.title,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = NaturalOnBackground
                                            )
                                        )
                                        Text(
                                            text = perk.description,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = NaturalOnSurfaceVariant,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = NaturalPrimaryContainer
                                    ) {
                                        Text(
                                            text = perk.category,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = NaturalPrimary,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun SocialInputRow(
    platform: String,
    value: String,
    onValueChange: (String) -> Unit,
    presets: List<Pair<String, Long>>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.55f))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = platform,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = NaturalOnBackground
            )
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NaturalPrimary,
                unfocusedBorderColor = NaturalOutline
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            presets.forEach { (chipLabel, amount) ->
                Button(
                    onClick = {
                        val cur = value.toLongOrNull() ?: 0L
                        val updated = if (chipLabel.startsWith("+")) cur + amount else amount
                        onValueChange(updated.toString())
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NaturalSecondaryContainer,
                        contentColor = NaturalPrimary
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)
                ) {
                    Text(chipLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ToggleItemRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.55f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = NaturalOnBackground
            )
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NaturalOnPrimary,
                checkedTrackColor = NaturalPrimary,
                uncheckedThumbColor = NaturalOutline,
                uncheckedTrackColor = NaturalSecondaryContainer
            )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BitLifeHeaderCard(
    scanResult: BitLifeSaveScanResult,
    selectedSlot: BitLifeSlotInfo?,
    selectedGame: com.example.bitlife.SupportedGame,
    isBusy: Boolean,
    hasShizukuPermission: Boolean,
    onAutoFind: () -> Unit,
    onAutoInjectGodLife: () -> Unit,
    onLaunchBitLife: () -> Unit,
    onRequestShizuku: () -> Unit,
    onScan: () -> Unit,
    onSelectGame: (com.example.bitlife.SupportedGame) -> Unit,
    onSelectSlot: (BitLifeSlotInfo) -> Unit,
    onPickFile: () -> Unit,
    onLaunchZArchiver: () -> Unit,
    isZArchiverInstalled: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bitlife_header_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = NaturalSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, NaturalOutline)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. GAME SELECTION ROW
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "SELECT TARGET GAME",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = NaturalPrimary,
                        letterSpacing = 0.5.sp
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    com.example.bitlife.BitLifeManager.SUPPORTED_GAMES.forEach { game ->
                        val isChosen = selectedGame.id == game.id
                        Card(
                            onClick = { onSelectGame(game) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isChosen) NaturalPrimaryContainer else NaturalSecondaryContainer
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isChosen) NaturalPrimary else NaturalOutlineVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("select_game_${game.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isChosen) NaturalPrimary else NaturalOutline),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = if (isChosen) NaturalOnPrimary else NaturalOnBackground,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = game.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isChosen) NaturalPrimary else NaturalOnBackground
                                        )
                                    )
                                    Text(
                                        text = "Target package: ${game.packageName}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = NaturalOnSurfaceVariant,
                                            fontSize = 10.sp
                                        )
                                    )
                                }

                                if (isChosen) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = NaturalPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = NaturalOutlineVariant.copy(alpha = 0.5f))

            // 2. HERO: 100% AUTOMATIC INJECTOR & INSTANT UNLOCK
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auto_save_finder_hero_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = NaturalPrimaryContainer),
                border = androidx.compose.foundation.BorderStroke(2.dp, NaturalPrimary)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(NaturalPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = NaturalOnPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "⚡ AUTOMATIC 1-TAP INJECTOR",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NaturalPrimary,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Text(
                                text = "Instantly writes God Mode, all DLCs & max stats directly into BitLife's directory.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = NaturalOnBackground.copy(alpha = 0.85f),
                                    lineHeight = 16.sp
                                )
                            )
                        }
                    }

                    // Main 1-Tap Auto-Inject Button
                    Button(
                        onClick = onAutoInjectGodLife,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("auto_inject_god_life_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NaturalPrimary,
                            contentColor = NaturalOnPrimary
                        ),
                        enabled = !isBusy
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBusy) "INJECTING INTO BITLIFE..." else "⚡ INJECT & UNLOCK ALL INTO BITLIFE",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }

                    // Shizuku permission notice if not yet authorized
                    if (!hasShizukuPermission) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = NaturalSecondaryContainer,
                            border = androidx.compose.foundation.BorderStroke(1.dp, NaturalOutlineVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = NaturalPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Grant Shizuku for 100% Zero-Touch Automation",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = NaturalOnBackground
                                        )
                                    )
                                    Text(
                                        text = "Allows reading & writing Android/data directly without manual file picking.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.sp,
                                            color = NaturalOnSurfaceVariant
                                        )
                                    )
                                }
                                Button(
                                    onClick = onRequestShizuku,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NaturalPrimary,
                                        contentColor = NaturalOnPrimary
                                    )
                                ) {
                                    Text("Grant", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Status and Quick Actions based on loaded slot
                    if (selectedSlot != null) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = NaturalSuccessContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = NaturalSuccess,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Auto-Loaded: ${selectedSlot.slotName}",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = NaturalOnBackground
                                            )
                                        )
                                        Text(
                                            text = selectedSlot.characterSummary ?: selectedSlot.filePath,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = NaturalOnBackground.copy(alpha = 0.8f),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = onLaunchBitLife,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(42.dp)
                                            .testTag("launch_bitlife_app_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, NaturalPrimary)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInNew,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = NaturalPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "🚀 Launch BitLife App",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = NaturalPrimary
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Quick Launch BitLife button
                        OutlinedButton(
                            onClick = onLaunchBitLife,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("launch_bitlife_app_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NaturalPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = NaturalPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "🚀 Launch BitLife App",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = NaturalPrimary
                            )
                        }
                    }
                }
            }

            // 3. COLLAPSIBLE ADVANCED MANUAL PICKER (ZArchiver / SAF Fallback)
            var showManualPicker by remember { mutableStateOf(false) }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("manual_file_picker_accordion_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NaturalSecondaryContainer.copy(alpha = 0.6f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, NaturalOutlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showManualPicker = !showManualPicker },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = NaturalOnSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Manual File Selection (ZArchiver Fallback)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = NaturalOnBackground
                                )
                            )
                        }
                        Text(
                            text = if (showManualPicker) "▲" else "▼",
                            color = NaturalOnSurfaceVariant,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    AnimatedVisibility(visible = showManualPicker) {
                        Column(
                            modifier = Modifier.padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Optional fallback: If you prefer picking savedLife.data manually with ZArchiver or Android Files:",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = NaturalOnSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onPickFile,
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .height(44.dp)
                                        .testTag("pick_save_file_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NaturalPrimary,
                                        contentColor = NaturalOnPrimary
                                    ),
                                    enabled = !isBusy
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FileOpen,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pick File (ZArchiver)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = onLaunchZArchiver,
                                    modifier = Modifier
                                        .weight(0.9f)
                                        .height(44.dp)
                                        .testTag("launch_zarchiver_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NaturalPrimary)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInNew,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        if (isZArchiverInstalled) "Open ZArchiver" else "Get ZArchiver",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. DETECTED SLOTS LIST (if multiple slots detected)
            if (scanResult.availableSlots.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "ALL DETECTED SAVE SLOTS (${scanResult.availableSlots.size})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NaturalPrimary,
                                letterSpacing = 0.5.sp
                            )
                        )
                        IconButton(
                            onClick = onScan,
                            enabled = !isBusy,
                            modifier = Modifier.size(28.dp).testTag("rescan_bitlife_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Rescan",
                                tint = NaturalPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    scanResult.availableSlots.forEach { slot ->
                        val isSelected = selectedSlot?.filePath == slot.filePath || (selectedSlot?.isPickedFile == true && slot.isPickedFile)
                        Card(
                            onClick = { onSelectSlot(slot) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) NaturalPrimaryContainer else NaturalSecondaryContainer
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) NaturalPrimary else NaturalOutlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("slot_chip_${slot.slotName.replace(" ", "_")}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.CheckCircle else if (slot.isPickedFile) Icons.Default.FileOpen else Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = if (isSelected) NaturalPrimary else NaturalOnSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = slot.slotName,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) NaturalPrimary else NaturalOnBackground
                                                )
                                            )
                                            if (slot.isPickedFile) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = NaturalPrimaryContainer
                                                ) {
                                                    Text(
                                                        text = "Picked File (ZArchiver)",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 9.sp,
                                                            color = NaturalPrimary,
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                    )
                                                }
                                            } else if (slot.isPrimaryActive) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = NaturalSuccessContainer
                                                ) {
                                                    Text(
                                                        text = "Active Life",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 9.sp,
                                                            color = NaturalSuccess,
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = slot.characterSummary,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = NaturalOnSurfaceVariant,
                                                fontSize = 12.sp
                                            )
                                        )

                                        if (slot.ageDataFiles.isNotEmpty()) {
                                            Text(
                                                text = "+ ${slot.ageDataFiles.size} previous age autosaves linked",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = NaturalPrimary.copy(alpha = 0.8f),
                                                    fontSize = 10.sp
                                                )
                                            )
                                        }
                                    }
                                }

                                if (isSelected) {
                                    Text(
                                        text = "SELECTED",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = NaturalPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Started Life guidance indicator
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = NaturalSecondaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = NaturalSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Started Life Verified (${scanResult.availableSlots.size} slot(s) found)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NaturalOnBackground
                                    )
                                )
                                Text(
                                    text = "Choose the character slot above that you are currently playing. Any stats you modify and save below will automatically apply to that specific life in BitLife.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = NaturalOnSurfaceVariant,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                )
                            }
                        }
                    }
                }
            } else {
                // No save found explanation
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = NaturalDangerContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassEmpty,
                                contentDescription = null,
                                tint = NaturalDanger,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "No Started Life Detected via Shizuku Scan",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NaturalDanger
                                )
                            )
                        }
                        Text(
                            text = "BitLife may not have created savedLife.data yet, or Shizuku did not find the directory. You can tap 'Select File (ZArchiver)' above to choose the file directly, or launch BitLife, age up once, and hit 'Rescan'.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = NaturalOnBackground,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        )

                        Button(
                            onClick = onPickFile,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("no_save_pick_file_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NaturalPrimary,
                                contentColor = NaturalOnPrimary
                            )
                        ) {
                            Icon(imageVector = Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Select savedLife.data with ZArchiver / Files", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCategoryCard(
    title: String,
    icon: ImageVector,
    description: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                        imageVector = icon,
                        contentDescription = null,
                        tint = NaturalPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = NaturalOnBackground
                        )
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NaturalOnSurfaceVariant,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            content()
        }
    }
}

@Composable
private fun StatSliderRow(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.55f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = NaturalOnBackground
                )
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "$value%",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = NaturalPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                )
                Button(
                    onClick = { onValueChange(100) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NaturalSecondaryContainer,
                        contentColor = NaturalPrimary
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("100%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..100f,
            steps = 99,
            colors = SliderDefaults.colors(
                thumbColor = NaturalPrimary,
                activeTrackColor = NaturalPrimary,
                inactiveTrackColor = NaturalOutlineVariant
            )
        )
    }
}

@Composable
private fun PresetChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.7f),
        border = androidx.compose.foundation.BorderStroke(1.dp, NaturalOutlineVariant)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = NaturalPrimary
            )
        )
    }
}
