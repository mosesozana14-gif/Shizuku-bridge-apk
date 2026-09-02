package com.example.bitlife

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

object BitLifeSavePatcher {

    /**
     * Attempts to parse current stats, social media numbers, and power-ups from raw save bytes.
     */
    fun parseStats(rawBytes: ByteArray): BitLifeStats {
        var stats = BitLifeStats()
        if (rawBytes.isEmpty()) return stats

        val contentStr = String(rawBytes, StandardCharsets.ISO_8859_1)

        // 1. Parse Text / XML / JSON based patterns if available
        stats = parseTextPatterns(contentStr, stats)

        // 2. Binary fallback / enhancement: Scan for serialized keys in byte stream
        stats = scanBinaryKeys(rawBytes, stats)

        return stats
    }

    /**
     * Patches raw save bytes with updated BitLifeStats, social media followers, and power-ups.
     */
    fun patchStats(rawBytes: ByteArray, newStats: BitLifeStats): ByteArray {
        if (rawBytes.isEmpty()) {
            return generateDefaultSave(newStats)
        }

        var resultBytes = rawBytes.copyOf()
        val contentStr = String(resultBytes, StandardCharsets.ISO_8859_1)

        val isTextFormat = contentStr.contains("<") && contentStr.contains(">") ||
                contentStr.contains("{") && contentStr.contains("}")

        if (isTextFormat) {
            resultBytes = patchTextContent(contentStr, newStats).toByteArray(StandardCharsets.ISO_8859_1)
        } else {
            resultBytes = patchBinaryContent(resultBytes, newStats)
        }

        return resultBytes
    }

    private fun parseTextPatterns(text: String, current: BitLifeStats): BitLifeStats {
        fun extractInt(vararg keys: String, default: Int): Int {
            for (key in keys) {
                val xmlRegex = Regex("""<$key>\s*(\d+)\s*</$key>""", RegexOption.IGNORE_CASE)
                xmlRegex.find(text)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }

                val jsonRegex = Regex(""""$key"\s*:\s*(\d+)""", RegexOption.IGNORE_CASE)
                jsonRegex.find(text)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }

                val kvRegex = Regex("""$key\s*=\s*(\d+)""", RegexOption.IGNORE_CASE)
                kvRegex.find(text)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
            }
            return default
        }

        fun extractLong(vararg keys: String, default: Long): Long {
            for (key in keys) {
                val xmlRegex = Regex("""<$key>\s*(\d+)\s*</$key>""", RegexOption.IGNORE_CASE)
                xmlRegex.find(text)?.groupValues?.get(1)?.toLongOrNull()?.let { return it }

                val jsonRegex = Regex(""""$key"\s*:\s*(\d+)""", RegexOption.IGNORE_CASE)
                jsonRegex.find(text)?.groupValues?.get(1)?.toLongOrNull()?.let { return it }

                val kvRegex = Regex("""$key\s*=\s*(\d+)""", RegexOption.IGNORE_CASE)
                kvRegex.find(text)?.groupValues?.get(1)?.toLongOrNull()?.let { return it }
            }
            return default
        }

        fun extractBool(vararg keys: String, default: Boolean): Boolean {
            for (key in keys) {
                val xmlRegex = Regex("""<$key>\s*(true|false|1|0)\s*</$key>""", RegexOption.IGNORE_CASE)
                val m = xmlRegex.find(text)?.groupValues?.get(1)
                if (m != null) return m.equals("true", true) || m == "1"

                val jsonRegex = Regex(""""$key"\s*:\s*(true|false|1|0)""", RegexOption.IGNORE_CASE)
                val jm = jsonRegex.find(text)?.groupValues?.get(1)
                if (jm != null) return jm.equals("true", true) || jm == "1"
            }
            return default
        }

        val parsedSocial = current.socialStats.copy(
            youtubeSubscribers = extractLong("YouTubeSubscribers", "m_youtubeSubscribers", "yt_subs", default = current.socialStats.youtubeSubscribers),
            tiktokFollowers = extractLong("TikTokFollowers", "m_tiktokFollowers", "tiktok_followers", default = current.socialStats.tiktokFollowers),
            instagramFollowers = extractLong("InstagramFollowers", "m_instagramFollowers", "ig_followers", default = current.socialStats.instagramFollowers),
            twitterFollowers = extractLong("TwitterFollowers", "m_twitterFollowers", "x_followers", default = current.socialStats.twitterFollowers),
            twitchFollowers = extractLong("TwitchFollowers", "m_twitchFollowers", "twitch_followers", default = current.socialStats.twitchFollowers),
            isVerified = extractBool("IsSocialVerified", "m_isVerified", "VerifiedBadge", default = current.socialStats.isVerified),
            viralBoost = extractBool("ViralBoost", "m_viralBoost", default = current.socialStats.viralBoost)
        )

        val parsedPowerUps = current.powerUps.copy(
            lotteryAutoWin = extractBool("LotteryAutoWin", "m_lotteryAutoWin", default = current.powerUps.lotteryAutoWin),
            casino100Win = extractBool("Casino100Win", "m_casino100Win", default = current.powerUps.casino100Win),
            crime100Success = extractBool("Crime100Success", "m_crime100Success", default = current.powerUps.crime100Success),
            prisonEscape100 = extractBool("PrisonEscape100", "m_prisonEscape100", default = current.powerUps.prisonEscape100),
            diseaseImmunity = extractBool("DiseaseImmunity", "m_diseaseImmunity", default = current.powerUps.diseaseImmunity),
            plasticSurgeryFlawless = extractBool("PlasticSurgeryFlawless", "m_plasticSurgeryFlawless", default = current.powerUps.plasticSurgeryFlawless),
            instantPromotionCEO = extractBool("InstantPromotionCEO", "m_instantPromotionCEO", default = current.powerUps.instantPromotionCEO),
            heirloomsUnlocked = extractBool("HeirloomsUnlocked", "m_heirloomsUnlocked", default = current.powerUps.heirloomsUnlocked),
            fertilityTwinsTriplets = extractBool("FertilityTwinsTriplets", "m_fertilityTwinsTriplets", default = current.powerUps.fertilityTwinsTriplets),
            unlimitedTimeMachine = extractBool("UnlimitedTimeMachine", "m_unlimitedTimeMachine", default = current.powerUps.unlimitedTimeMachine)
        )

        return current.copy(
            happiness = extractInt("Happiness", "m_happiness", "happiness", default = current.happiness),
            health = extractInt("Health", "m_health", "health", default = current.health),
            smarts = extractInt("Smarts", "m_smarts", "smarts", "Intelligence", default = current.smarts),
            looks = extractInt("Looks", "m_looks", "looks", "Appearance", default = current.looks),
            karma = extractInt("Karma", "m_karma", "karma", default = current.karma),
            fame = extractInt("Fame", "m_fame", "fame", default = current.fame),
            athleticism = extractInt("Athleticism", "m_athleticism", "athleticism", "Athletics", default = current.athleticism),
            discipline = extractInt("Discipline", "m_discipline", "discipline", default = current.discipline),
            craziness = extractInt("Craziness", "m_craziness", "craziness", default = current.craziness),
            willpower = extractInt("Willpower", "m_willpower", "willpower", default = current.willpower),
            musicTalent = extractInt("Music", "m_music", "musicTalent", "MusicTalent", default = current.musicTalent),
            actingTalent = extractInt("Acting", "m_acting", "actingTalent", "ActingTalent", default = current.actingTalent),
            voiceTalent = extractInt("Voice", "m_voice", "voiceTalent", "Singing", default = current.voiceTalent),
            streetSmarts = extractInt("StreetSmarts", "m_streetSmarts", "streetSmarts", default = current.streetSmarts),
            fertility = extractInt("Fertility", "m_fertility", "fertility", default = current.fertility),
            generosity = extractInt("Generosity", "m_generosity", "generosity", default = current.generosity),
            age = extractInt("Age", "m_age", "age", default = current.age),
            ageAtDeath = extractInt("ageAtDeath", "m_ageAtDeath", "AgeAtDeath", "Lifespan", default = current.ageAtDeath),
            bankBalance = extractLong("BankBalance", "m_bankBalance", "bankBalance", "Money", "Balance", default = current.bankBalance),
            salary = extractLong("Salary", "m_salary", "salary", "YearlyPay", default = current.salary),
            jobPerformance = extractInt("JobPerformance", "m_jobPerformance", "jobPerformance", "Performance", default = current.jobPerformance),
            schoolGrades = extractInt("SchoolGrades", "m_schoolGrades", "schoolGrades", "Grades", default = current.schoolGrades),
            socialStats = parsedSocial,
            powerUps = parsedPowerUps
        )
    }

    private fun patchTextContent(originalText: String, stats: BitLifeStats): String {
        var modified = originalText

        fun replaceField(keys: List<String>, newValue: String) {
            for (key in keys) {
                val xmlRegex = Regex("""(<$key>)\s*[^<]+\s*(</$key>)""", RegexOption.IGNORE_CASE)
                if (xmlRegex.containsMatchIn(modified)) {
                    modified = xmlRegex.replace(modified, "$1$newValue$2")
                    return
                }

                val jsonRegex = Regex("""("$key"\s*:\s*)[^,}\n]+""", RegexOption.IGNORE_CASE)
                if (jsonRegex.containsMatchIn(modified)) {
                    modified = jsonRegex.replace(modified, "$1$newValue")
                    return
                }

                val kvRegex = Regex("""($key\s*=\s*)[^\n]+""", RegexOption.IGNORE_CASE)
                if (kvRegex.containsMatchIn(modified)) {
                    modified = kvRegex.replace(modified, "$1$newValue")
                    return
                }
            }
        }

        // Core stats
        replaceField(listOf("Happiness", "m_happiness", "happiness"), stats.happiness.toString())
        replaceField(listOf("Health", "m_health", "health"), stats.health.toString())
        replaceField(listOf("Smarts", "m_smarts", "smarts", "Intelligence"), stats.smarts.toString())
        replaceField(listOf("Looks", "m_looks", "looks", "Appearance"), stats.looks.toString())
        replaceField(listOf("Karma", "m_karma", "karma"), stats.karma.toString())
        replaceField(listOf("Fame", "m_fame", "fame"), stats.fame.toString())
        replaceField(listOf("Athleticism", "m_athleticism", "athleticism", "Athletics"), stats.athleticism.toString())
        replaceField(listOf("Discipline", "m_discipline", "discipline"), stats.discipline.toString())
        replaceField(listOf("Craziness", "m_craziness", "craziness"), stats.craziness.toString())
        replaceField(listOf("Willpower", "m_willpower", "willpower"), stats.willpower.toString())
        replaceField(listOf("Music", "m_music", "musicTalent"), stats.musicTalent.toString())
        replaceField(listOf("Acting", "m_acting", "actingTalent"), stats.actingTalent.toString())
        replaceField(listOf("Voice", "m_voice", "voiceTalent"), stats.voiceTalent.toString())
        replaceField(listOf("StreetSmarts", "m_streetSmarts", "streetSmarts"), stats.streetSmarts.toString())
        replaceField(listOf("Fertility", "m_fertility", "fertility"), stats.fertility.toString())
        replaceField(listOf("Generosity", "m_generosity", "generosity"), stats.generosity.toString())
        replaceField(listOf("Age", "m_age", "age"), stats.age.toString())
        replaceField(listOf("ageAtDeath", "m_ageAtDeath", "AgeAtDeath", "Lifespan"), stats.ageAtDeath.toString())
        replaceField(listOf("BankBalance", "m_bankBalance", "bankBalance", "Money", "Balance"), stats.bankBalance.toString())
        replaceField(listOf("Salary", "m_salary", "salary", "YearlyPay"), stats.salary.toString())
        replaceField(listOf("JobPerformance", "m_jobPerformance", "jobPerformance", "Performance"), stats.jobPerformance.toString())
        replaceField(listOf("SchoolGrades", "m_schoolGrades", "schoolGrades", "Grades"), stats.schoolGrades.toString())

        // Social Media & Moses Mod Menu stats
        replaceField(listOf("YouTubeSubscribers", "m_youtubeSubscribers", "yt_subs"), stats.socialStats.youtubeSubscribers.toString())
        replaceField(listOf("TikTokFollowers", "m_tiktokFollowers", "tiktok_followers"), stats.socialStats.tiktokFollowers.toString())
        replaceField(listOf("InstagramFollowers", "m_instagramFollowers", "ig_followers"), stats.socialStats.instagramFollowers.toString())
        replaceField(listOf("TwitterFollowers", "m_twitterFollowers", "x_followers"), stats.socialStats.twitterFollowers.toString())
        replaceField(listOf("TwitchFollowers", "m_twitchFollowers", "twitch_followers"), stats.socialStats.twitchFollowers.toString())
        replaceField(listOf("IsSocialVerified", "m_isVerified", "VerifiedBadge"), stats.socialStats.isVerified.toString())
        replaceField(listOf("ViralBoost", "m_viralBoost"), stats.socialStats.viralBoost.toString())

        // Power-Ups
        replaceField(listOf("LotteryAutoWin", "m_lotteryAutoWin"), stats.powerUps.lotteryAutoWin.toString())
        replaceField(listOf("Casino100Win", "m_casino100Win"), stats.powerUps.casino100Win.toString())
        replaceField(listOf("Crime100Success", "m_crime100Success"), stats.powerUps.crime100Success.toString())
        replaceField(listOf("PrisonEscape100", "m_prisonEscape100"), stats.powerUps.prisonEscape100.toString())
        replaceField(listOf("DiseaseImmunity", "m_diseaseImmunity"), stats.powerUps.diseaseImmunity.toString())
        replaceField(listOf("PlasticSurgeryFlawless", "m_plasticSurgeryFlawless"), stats.powerUps.plasticSurgeryFlawless.toString())
        replaceField(listOf("InstantPromotionCEO", "m_instantPromotionCEO"), stats.powerUps.instantPromotionCEO.toString())
        replaceField(listOf("HeirloomsUnlocked", "m_heirloomsUnlocked"), stats.powerUps.heirloomsUnlocked.toString())
        replaceField(listOf("FertilityTwinsTriplets", "m_fertilityTwinsTriplets"), stats.powerUps.fertilityTwinsTriplets.toString())
        replaceField(listOf("RoyaltyRank", "m_royaltyRank"), "\"${stats.powerUps.royaltyRank}\"")

        return modified
    }

    private fun scanBinaryKeys(raw: ByteArray, current: BitLifeStats): BitLifeStats {
        val str = String(raw, StandardCharsets.ISO_8859_1)
        var res = current

        fun findBinaryInt(key: String): Int? {
            val idx = str.indexOf(key)
            if (idx != -1) {
                val offset = idx + key.length
                if (offset + 4 <= raw.size) {
                    val buffer = ByteBuffer.wrap(raw, offset, 4).order(ByteOrder.LITTLE_ENDIAN)
                    val v = buffer.int
                    if (v in 0..1000) return v
                }
            }
            return null
        }

        fun findBinaryLong(key: String): Long? {
            val idx = str.indexOf(key)
            if (idx != -1) {
                val offset = idx + key.length
                if (offset + 8 <= raw.size) {
                    val buffer = ByteBuffer.wrap(raw, offset, 8).order(ByteOrder.LITTLE_ENDIAN)
                    val v = buffer.long
                    if (v in 0..1_000_000_000_000_000L) return v
                }
            }
            return null
        }

        findBinaryInt("m_happiness")?.let { res = res.copy(happiness = it) }
        findBinaryInt("m_health")?.let { res = res.copy(health = it) }
        findBinaryInt("m_smarts")?.let { res = res.copy(smarts = it) }
        findBinaryInt("m_looks")?.let { res = res.copy(looks = it) }
        findBinaryInt("m_karma")?.let { res = res.copy(karma = it) }
        findBinaryInt("m_fame")?.let { res = res.copy(fame = it) }
        findBinaryInt("m_athleticism")?.let { res = res.copy(athleticism = it) }
        findBinaryInt("m_discipline")?.let { res = res.copy(discipline = it) }
        findBinaryLong("m_bankBalance")?.let { res = res.copy(bankBalance = it) }

        // Social keys in binary
        var soc = res.socialStats
        findBinaryLong("m_youtubeSubscribers")?.let { soc = soc.copy(youtubeSubscribers = it) }
        findBinaryLong("m_tiktokFollowers")?.let { soc = soc.copy(tiktokFollowers = it) }
        findBinaryLong("m_instagramFollowers")?.let { soc = soc.copy(instagramFollowers = it) }
        res = res.copy(socialStats = soc)

        return res
    }

    private fun patchBinaryContent(raw: ByteArray, stats: BitLifeStats): ByteArray {
        val str = String(raw, StandardCharsets.ISO_8859_1)
        val copy = raw.copyOf()

        fun patchBinaryInt(key: String, value: Int) {
            var searchFrom = 0
            while (true) {
                val idx = str.indexOf(key, searchFrom)
                if (idx == -1) break
                val offset = idx + key.length
                if (offset + 4 <= copy.size) {
                    ByteBuffer.wrap(copy, offset, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(value)
                }
                searchFrom = idx + key.length
            }
        }

        fun patchBinaryLong(key: String, value: Long) {
            var searchFrom = 0
            while (true) {
                val idx = str.indexOf(key, searchFrom)
                if (idx == -1) break
                val offset = idx + key.length
                if (offset + 8 <= copy.size) {
                    ByteBuffer.wrap(copy, offset, 8).order(ByteOrder.LITTLE_ENDIAN).putLong(value)
                }
                searchFrom = idx + key.length
            }
        }

        // Apply binary keys
        listOf("m_happiness", "Happiness").forEach { patchBinaryInt(it, stats.happiness) }
        listOf("m_health", "Health").forEach { patchBinaryInt(it, stats.health) }
        listOf("m_smarts", "Smarts").forEach { patchBinaryInt(it, stats.smarts) }
        listOf("m_looks", "Looks").forEach { patchBinaryInt(it, stats.looks) }
        listOf("m_karma", "Karma").forEach { patchBinaryInt(it, stats.karma) }
        listOf("m_fame", "Fame").forEach { patchBinaryInt(it, stats.fame) }
        listOf("m_athleticism", "Athleticism").forEach { patchBinaryInt(it, stats.athleticism) }
        listOf("m_discipline", "Discipline").forEach { patchBinaryInt(it, stats.discipline) }
        listOf("m_craziness", "Craziness").forEach { patchBinaryInt(it, stats.craziness) }
        listOf("m_willpower", "Willpower").forEach { patchBinaryInt(it, stats.willpower) }
        listOf("m_age", "Age").forEach { patchBinaryInt(it, stats.age) }
        listOf("m_ageAtDeath", "ageAtDeath").forEach { patchBinaryInt(it, stats.ageAtDeath) }
        listOf("m_bankBalance", "BankBalance").forEach { patchBinaryLong(it, stats.bankBalance) }
        listOf("m_salary", "Salary").forEach { patchBinaryLong(it, stats.salary) }

        // Social binary keys
        listOf("m_youtubeSubscribers", "YouTubeSubscribers").forEach { patchBinaryLong(it, stats.socialStats.youtubeSubscribers) }
        listOf("m_tiktokFollowers", "TikTokFollowers").forEach { patchBinaryLong(it, stats.socialStats.tiktokFollowers) }
        listOf("m_instagramFollowers", "InstagramFollowers").forEach { patchBinaryLong(it, stats.socialStats.instagramFollowers) }
        listOf("m_twitterFollowers", "TwitterFollowers").forEach { patchBinaryLong(it, stats.socialStats.twitterFollowers) }
        listOf("m_twitchFollowers", "TwitchFollowers").forEach { patchBinaryLong(it, stats.socialStats.twitchFollowers) }

        return copy
    }

    private fun generateDefaultSave(stats: BitLifeStats): ByteArray {
        val xml = buildString {
            append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
            append("<SavedLife>\n")
            append("  <Hero>\n")
            append("    <Age>${stats.age}</Age>\n")
            append("    <ageAtDeath>${stats.ageAtDeath}</ageAtDeath>\n")
            append("    <Happiness>${stats.happiness}</Happiness>\n")
            append("    <Health>${stats.health}</Health>\n")
            append("    <Smarts>${stats.smarts}</Smarts>\n")
            append("    <Looks>${stats.looks}</Looks>\n")
            append("    <Karma>${stats.karma}</Karma>\n")
            append("    <Fame>${stats.fame}</Fame>\n")
            append("    <Athleticism>${stats.athleticism}</Athleticism>\n")
            append("    <Discipline>${stats.discipline}</Discipline>\n")
            append("    <Craziness>${stats.craziness}</Craziness>\n")
            append("    <Willpower>${stats.willpower}</Willpower>\n")
            append("    <Music>${stats.musicTalent}</Music>\n")
            append("    <Acting>${stats.actingTalent}</Acting>\n")
            append("    <Voice>${stats.voiceTalent}</Voice>\n")
            append("    <StreetSmarts>${stats.streetSmarts}</StreetSmarts>\n")
            append("    <Fertility>${stats.fertility}</Fertility>\n")
            append("    <Generosity>${stats.generosity}</Generosity>\n")
            append("  </Hero>\n")
            append("  <Finances>\n")
            append("    <BankBalance>${stats.bankBalance}</BankBalance>\n")
            append("    <Salary>${stats.salary}</Salary>\n")
            append("    <JobPerformance>${stats.jobPerformance}</JobPerformance>\n")
            append("    <SchoolGrades>${stats.schoolGrades}</SchoolGrades>\n")
            append("  </Finances>\n")
            append("  <SocialMedia>\n")
            append("    <YouTubeSubscribers>${stats.socialStats.youtubeSubscribers}</YouTubeSubscribers>\n")
            append("    <TikTokFollowers>${stats.socialStats.tiktokFollowers}</TikTokFollowers>\n")
            append("    <InstagramFollowers>${stats.socialStats.instagramFollowers}</InstagramFollowers>\n")
            append("    <TwitterFollowers>${stats.socialStats.twitterFollowers}</TwitterFollowers>\n")
            append("    <TwitchFollowers>${stats.socialStats.twitchFollowers}</TwitchFollowers>\n")
            append("    <IsSocialVerified>${stats.socialStats.isVerified}</IsSocialVerified>\n")
            append("    <ViralBoost>${stats.socialStats.viralBoost}</ViralBoost>\n")
            append("  </SocialMedia>\n")
            append("  <PowerUps>\n")
            append("    <LotteryAutoWin>${stats.powerUps.lotteryAutoWin}</LotteryAutoWin>\n")
            append("    <Casino100Win>${stats.powerUps.casino100Win}</Casino100Win>\n")
            append("    <Crime100Success>${stats.powerUps.crime100Success}</Crime100Success>\n")
            append("    <PrisonEscape100>${stats.powerUps.prisonEscape100}</PrisonEscape100>\n")
            append("    <DiseaseImmunity>${stats.powerUps.diseaseImmunity}</DiseaseImmunity>\n")
            append("    <PlasticSurgeryFlawless>${stats.powerUps.plasticSurgeryFlawless}</PlasticSurgeryFlawless>\n")
            append("    <InstantPromotionCEO>${stats.powerUps.instantPromotionCEO}</InstantPromotionCEO>\n")
            append("    <HeirloomsUnlocked>${stats.powerUps.heirloomsUnlocked}</HeirloomsUnlocked>\n")
            append("    <FertilityTwinsTriplets>${stats.powerUps.fertilityTwinsTriplets}</FertilityTwinsTriplets>\n")
            append("    <RoyaltyRank>${stats.powerUps.royaltyRank}</RoyaltyRank>\n")
            append("  </PowerUps>\n")
            append("</SavedLife>\n")
        }
        return xml.toByteArray(StandardCharsets.UTF_8)
    }

    /**
     * Generates a fully unlocked MonetizationVars payload unlocking EVERY expansion, pack, perk and item in BitLife history.
     */
    /**
     * Generates MonetizationVars payload with ALL God Mode, Bitizenship, and DLC flags.
     * Generates a clean JSON structure and dual fallback key-values compatible with modern BitLife.
     */
    fun generateGodModeMonetizationVars(): ByteArray {
        val keys = listOf(
            "Bitizen",
            "Bitizenship",
            "GodMode",
            "BossMode",
            "UnlimitedTimeMachine",
            "TimeMachine",
            "ChallengeVault",
            "SuperstarVIP",
            "NoAds",
            "RemoveAds",
            "StreakSaver",
            "CelebrityDatingApp",
            "GoldenPassport",
            "HollywoodStar",
            "AssassinBlade",
            "AssassinsBlade",
            "BrassKnuckles",
            "GoldenWrench",
            "GoldenDiploma",
            "GoldenResume",
            "PromisingInstrument",
            "HolyGrailItem",
            "LandlordExpansion",
            "CultExpansion",
            "BlackMarketExpansion",
            "ZooExpansion",
            "SecretAgentExpansion",
            "SecretAgentJobPack",
            "ModelJobPack",
            "DealerJobPack",
            "WeedDispensaryExpansion",
            "VampireJobPack",
            "ActorJobPack",
            "MusicianJobPack",
            "AthleteJobPack",
            "ProAthleteJobPack",
            "PoliticianJobPack",
            "BusinessJobPack",
            "StreetHustlerJobPack",
            "MafiaJobPack",
            "AstronautJobPack",
            "AllJobPacksUnlocked",
            "AllExpansionsUnlocked",
            "AllItemsUnlocked",
            "GodModeAllNPCs"
        )

        // Generate JSON dictionary format: {"Bitizen":true,"Bitizenship":true,...}
        val jsonEntries = keys.joinToString(",") { "\"$it\":true" }
        val jsonPayload = "{$jsonEntries}"

        return jsonPayload.toByteArray(StandardCharsets.UTF_8)
    }

    /**
     * List of all official BitLife Perks & Expansions with descriptions for UI showcase.
     */
    fun getAllPerksList(): List<BitLifePerkInfo> = listOf(
        BitLifePerkInfo("Bitizenship", "Removes all ads, grants unlimited generations, pet breeds & dark mode", "Membership"),
        BitLifePerkInfo("God Mode", "Full editor for all characters, looks, stats, fertility, sexuality & craziness", "Editor"),
        BitLifePerkInfo("Boss Mode", "Unlocks all current and future Career & Job Packs forever", "Bundle"),
        BitLifePerkInfo("Unlimited Time Machine", "Travel back in time years whenever you want with zero cost", "Power-Up"),
        BitLifePerkInfo("Golden Passport", "Emigrate to any country globally with 100% acceptance & no fees", "Item"),
        BitLifePerkInfo("Hollywood Star", "Instant 100% Fame & automatic A-List celebrity status in careers", "Item"),
        BitLifePerkInfo("Assassin's Blade", "Guaranteed 100% immune to police detection on all crimes & murders", "Item"),
        BitLifePerkInfo("Brass Knuckles", "Guarantees maximum attack power and street fight victories", "Item"),
        BitLifePerkInfo("Golden Wrench", "Vehicles, supercars, yachts and planes never break down", "Item"),
        BitLifePerkInfo("Golden Diploma & Resume", "Never get rejected from universities or executive job applications", "Item"),
        BitLifePerkInfo("Challenge Vault", "Unlimited access to all past BitLife weekly challenges & rare crowns", "Vault"),
        BitLifePerkInfo("Landlord Expansion", "Buy, furnish, rent real estate properties, screen tenants & evict", "Expansion"),
        BitLifePerkInfo("Cult Expansion", "Create your own commune, recruit followers, build compound & doctrines", "Expansion"),
        BitLifePerkInfo("Black Market Expansion", "Auction houses, illicit antiquities, private gallery & black market deals", "Expansion"),
        BitLifePerkInfo("Zoo Expansion", "Build world-class zoo attractions, breed exotic beasts & manage staff", "Expansion"),
        BitLifePerkInfo("Secret Agent / Spy Pack", "Run covert spy agency, infiltrate syndicates, wiretap & assassinate", "Expansion"),
        BitLifePerkInfo("Model Job Pack", "Walk fashion runways, shoot magazine covers & become Supermodel", "Job Pack"),
        BitLifePerkInfo("Dealer / Weed Dispensary Pack", "Run street and dispensary operations, mix products & control turf", "Expansion"),
        BitLifePerkInfo("Vampire Job Pack", "Drink blood, live for millennia, turn thralls & avoid sunlight", "Job Pack"),
        BitLifePerkInfo("Astronaut Job Pack", "Train at NASA/Space Agency, launch into orbit, walk on Mars & Moon", "Job Pack"),
        BitLifePerkInfo("Actor Job Pack", "Audition for TV & Hollywood blockbusters, win awards & direct films", "Job Pack"),
        BitLifePerkInfo("Musician (Pop Star) Pack", "Sign record deals, drop Platinum albums, tour stadiums world-wide", "Job Pack"),
        BitLifePerkInfo("Pro Athlete Pack", "Draft to NFL, NBA, Premier League, win championship rings & MVPs", "Job Pack"),
        BitLifePerkInfo("Politician Pack", "Run campaigns, get elected School Board, Mayor, Governor & President", "Job Pack"),
        BitLifePerkInfo("Business Job Pack", "Found startup companies, conduct IPOs, manage supply chain & acquire firms", "Job Pack"),
        BitLifePerkInfo("Street Hustler Pack", "Perform street scams, busking, panhandling & dominate street corners", "Job Pack"),
        BitLifePerkInfo("Mafia Job Pack", "Join Yakuza, Cartel, Cosa Nostra, extort businesses & become Godfather", "Job Pack"),
        BitLifePerkInfo("Celebrity Dating App", "Date billionaires, movie stars, royalty & supermodels exclusively", "Addon")
    )
}

data class BitLifePerkInfo(
    val title: String,
    val description: String,
    val category: String
)
