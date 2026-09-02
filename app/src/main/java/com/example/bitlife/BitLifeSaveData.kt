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
    val youtubeSubscribers: Long = 0L,
    val tiktokFollowers: Long = 0L,
    val instagramFollowers: Long = 0L,
    val twitterFollowers: Long = 0L,
    val twitchFollowers: Long = 0L,
    val isVerified: Boolean = false,
    val viralBoost: Boolean = false
)

data class BitLifePowerUps(
    val lotteryAutoWin: Boolean = false,
    val casino100Win: Boolean = false,
    val crime100Success: Boolean = false,
    val prisonEscape100: Boolean = false,
    val diseaseImmunity: Boolean = false,
    val plasticSurgeryFlawless: Boolean = false,
    val instantPromotionCEO: Boolean = false,
    val heirloomsUnlocked: Boolean = false,
    val fertilityTwinsTriplets: Boolean = false,
    val unlimitedTimeMachine: Boolean = false,
    val cryptoStockMarketMaster: Boolean = false,
    val luxuryEstateSupercars: Boolean = false,
    val ultraViralBoost: Boolean = false,
    val royaltyRank: String = "None" // None, Prince, King, Emperor, Duke
)

data class BitLifeStats(
    // Core Attributes (0 - 100)
    val happiness: Int = 100,
    val health: Int = 100,
    val smarts: Int = 100,
    val looks: Int = 100,

    // Hidden / Secondary Attributes (0 - 100)
    val karma: Int = 50,
    val fame: Int = 0,
    val athleticism: Int = 50,
    val discipline: Int = 50,
    val craziness: Int = 0,
    val willpower: Int = 50,
    val musicTalent: Int = 0,
    val actingTalent: Int = 0,
    val voiceTalent: Int = 0,
    val streetSmarts: Int = 0,
    val fertility: Int = 50,
    val generosity: Int = 50,

    // Longevity & Age
    val age: Int = 20,
    val ageAtDeath: Int = 100, // Standard Lifespan (not 1000)

    // Finances & Career
    val bankBalance: Long = 0L, // 0 cash by default so player sets custom amount
    val salary: Long = 0L,
    val jobPerformance: Int = 50,
    val schoolGrades: Int = 50,

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
