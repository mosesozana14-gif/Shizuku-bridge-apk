package com.example.bitlife

data class BitLifeSlotInfo(
    val slotName: String, // e.g., "sg1", "sg2", "Main Save (Root)", "Picked from ZArchiver"
    val filePath: String, // e.g., "/storage/emulated/0/Android/data/com.candywriter.bitlife/files/sg1/savedLife.data"
    val contentUri: android.net.Uri? = null,
    val ageDataFiles: List<String> = emptyList(),
    val lastModified: Long = 0L,
    val fileSize: Long = 0L,
    val characterSummary: String = "", // e.g., "Age 22 • $10.5M • 100% Health"
    val isPrimaryActive: Boolean = false,
    val isPickedFile: Boolean = false,
    val isMonetizationVars: Boolean = false
)

data class SupportedGame(
    val id: String,
    val name: String,
    val packageName: String,
    val iconName: String,
    val isAvailable: Boolean = true,
    val description: String = ""
)

data class BitLifeSocialStats(
    val youtubeSubscribers: Long = 10_000_000L,
    val tiktokFollowers: Long = 5_000_000L,
    val instagramFollowers: Long = 8_000_000L,
    val twitterFollowers: Long = 3_000_000L,
    val twitchFollowers: Long = 1_500_000L,
    val isVerified: Boolean = true,
    val viralBoost: Boolean = true
)

data class BitLifePowerUps(
    val lotteryAutoWin: Boolean = true,
    val casino100Win: Boolean = true,
    val crime100Success: Boolean = true,
    val prisonEscape100: Boolean = true,
    val diseaseImmunity: Boolean = true,
    val plasticSurgeryFlawless: Boolean = true,
    val instantPromotionCEO: Boolean = true,
    val heirloomsUnlocked: Boolean = true,
    val fertilityTwinsTriplets: Boolean = true,
    val unlimitedTimeMachine: Boolean = true,
    val royaltyRank: String = "Prince" // None, Prince, King, Emperor, Duke
)

data class BitLifeStats(
    // Core Attributes (0 - 100)
    val happiness: Int = 100,
    val health: Int = 100,
    val smarts: Int = 100,
    val looks: Int = 100,

    // Hidden / Secondary Attributes (0 - 100)
    val karma: Int = 100,
    val fame: Int = 100,
    val athleticism: Int = 100,
    val discipline: Int = 100,
    val craziness: Int = 0,
    val willpower: Int = 100,
    val musicTalent: Int = 100,
    val actingTalent: Int = 100,
    val voiceTalent: Int = 100,
    val streetSmarts: Int = 100,
    val fertility: Int = 100,
    val generosity: Int = 100,

    // Longevity & Age
    val age: Int = 20,
    val ageAtDeath: Int = 1000, // Lifespan / Immortality

    // Finances & Career
    val bankBalance: Long = 100_000_000L,
    val salary: Long = 1_000_000L,
    val jobPerformance: Int = 100,
    val schoolGrades: Int = 100,

    // Moses Mod Menu Features
    val socialStats: BitLifeSocialStats = BitLifeSocialStats(),
    val powerUps: BitLifePowerUps = BitLifePowerUps()
)

data class BitLifeSaveScanResult(
    val isAppInstalled: Boolean = false,
    val baseDirFound: String = "",
    val availableSlots: List<BitLifeSlotInfo> = emptyList(),
    val hasMonetizationVars: Boolean = false,
    val error: String = ""
)
