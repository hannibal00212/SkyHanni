package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage.HoppityEventStatsStorage
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.hoppity.RabbitFoundEvent
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.CollectionUtils.sumAllValues
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.SkyBlockTime
import at.hannibal2.skyhanni.utils.SkyBlockTime.Companion.SKYBLOCK_DAY_MILLIS
import at.hannibal2.skyhanni.utils.SkyBlockTime.Companion.SKYBLOCK_HOUR_MILLIS
import at.hannibal2.skyhanni.utils.SkyblockSeason
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TimeUtils.format
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object HoppityEventSummary {
    // First event was year 346 -> #1, 20th event was year 365, etc.
    private fun getHoppityEventNumber(skyblockYear: Int): Int = (skyblockYear - 345)
    private val config get() = SkyHanniMod.feature.event.hoppityEggs
    private val lineHeader = " ".repeat(4)

    @HandleEvent
    fun onRabbitFound(event: RabbitFoundEvent) {
        if (!HoppityAPI.isHoppityEvent()) return
        val stats = ProfileStorageData.profileSpecific?.hoppityEventStats ?: return

        stats.mealTypeMap.addOrPut(event.eggType, 1)
        val rarity = HoppityRabbitRarity.getByRabbit(event.rabbitName) ?: return
        if (event.duplicate) stats.dupeRarityMap.addOrPut(rarity, 1)
        else stats.newRarityMap.addOrPut(rarity, 1)
        if (event.chocGained > 0) stats.chocolateGained += event.chocGained
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!LorenzUtils.inSkyBlock) return
        checkEnded()
    }

    @SubscribeEvent
    fun onProfileJoin(event: ProfileJoinEvent) {
        checkEnded()
    }

    fun forceEventEnd() {
        ProfileStorageData.profileSpecific?.hoppityEventStats?.currentYear ?: return
        val currentYear = SkyBlockTime.now().year
        ProfileStorageData.profileSpecific?.hoppityEventStats!!.currentYear = currentYear - 1
    }

    private fun checkEnded() {
        if (!config.eventSummary) return
        val stats = ProfileStorageData.profileSpecific?.hoppityEventStats ?: return

        val currentYear = SkyBlockTime.now().year
        val currentSeason = SkyblockSeason.currentSeason

        if (stats == HoppityEventStatsStorage()) {
            ProfileStorageData.profileSpecific!!.hoppityEventStats!!.currentYear = currentYear
            return
        }

        val ended = stats.currentYear < currentYear || (stats.currentYear == currentYear && currentSeason != SkyblockSeason.SPRING)
        if (ended) {
            sendSummaryMessage(SummaryType.CONCLUDED, stats)
            ProfileStorageData.profileSpecific?.hoppityEventStats = HoppityEventStatsStorage()
            ProfileStorageData.profileSpecific?.hoppityEventStats!!.currentYear = currentYear + 1
        }
    }

    enum class SummaryType(val displayName: String) {
        CONCLUDED("Concluded"),
        PROGRESS("Progress")
        ;

        override fun toString(): String = displayName
    }

    fun sendProgressMessage() {
        if (!HoppityAPI.isHoppityEvent()) {
            ChatUtils.chat("§eThis command only works while §d§lHoppity's Hunt §eis active.", prefix = false)
            return
        }
        val stats = ProfileStorageData.profileSpecific?.hoppityEventStats
            ?: ErrorManager.skyHanniError("Could not read stats for current Hoppity's Event")

        sendSummaryMessage(SummaryType.PROGRESS, stats)
    }

    fun addStrayCaught(rarity: HoppityRabbitRarity, chocGained: Long) {
        val stats = ProfileStorageData.profileSpecific?.hoppityEventStats ?: return
        stats.strayRarityMap.addOrPut(rarity, 1)
        stats.strayChocolateGained += chocGained
    }

    private fun sendSummaryMessage(type: SummaryType, stats: HoppityEventStatsStorage) {
        val headerMessage = "§d§lHoppity's Hunt #${getHoppityEventNumber(stats.currentYear)} $type"
        val headerLength = headerMessage.removeColor().length
        val wrapperLength = ((headerLength + 8) * 1.5).toInt()
        val summaryWrapper = "§d§l${"▬".repeat(wrapperLength + 4)}"

        val summaryBuilder: StringBuilder = StringBuilder()
        summaryBuilder.appendLine(summaryWrapper)

        // Header
        summaryBuilder.appendLine("${" ".repeat(((summaryWrapper.length - 4) / 2) - 8)}$headerMessage")
        summaryBuilder.appendLine()
        // Eggs found
        stats.getEggsFoundFormat().forEach { summaryBuilder.appendHeadedLine(it) }
        summaryBuilder.appendLine()
        // New rabbits
        getRabbitsFormat(stats.newRarityMap, "Unique", "§b").forEach { summaryBuilder.appendHeadedLine(it) }
        if (stats.newRarityMap.any()) summaryBuilder.appendLine()
        // Dupe rabbits
        getRabbitsFormat(stats.dupeRarityMap, "Duplicate", "§c").forEach { summaryBuilder.appendHeadedLine(it) }
        summaryBuilder.addExtraChocFormatLine(stats.chocolateGained)
        if (stats.dupeRarityMap.any()) summaryBuilder.appendLine()
        // Stray rabbits
        getRabbitsFormat(stats.strayRarityMap, "Stray", "§f").forEach { summaryBuilder.appendHeadedLine(it) }
        summaryBuilder.addExtraChocFormatLine(stats.strayChocolateGained)
        if (stats.strayRarityMap.any()) summaryBuilder.appendLine()

        summaryBuilder.appendLine(summaryWrapper)

        ChatUtils.chat(summaryBuilder.toString(), prefix = false)
    }

    private fun StringBuilder.appendHeadedLine(line: String) {
        appendLine("$lineHeader$line")
    }

    private fun StringBuilder.addExtraChocFormatLine(chocGained: Long) {
        if (chocGained <= 0) return
        var extraChocFormat = " §6+${chocGained.addSeparators()} Chocolate"
        if (SkyHanniMod.feature.inventory.chocolateFactory.showDuplicateTime) {
            val timeFormatted = ChocolateFactoryAPI.timeUntilNeed(chocGained).format(maxUnits = 2)
            extraChocFormat += " §7(§a+§b${timeFormatted}§7)"
        }
        appendHeadedLine(extraChocFormat)
    }

    private fun HoppityEventStatsStorage.getEggsFoundFormat(): List<String> {
        val eggsFoundFormatList = mutableListOf<String>()
        val foundMealEggs = mealTypeMap.filterKeys { HoppityEggType.resettingEntries.contains(it) }.sumAllValues().toInt()
        if (foundMealEggs > 0) {
            val spawnedEggs = getMealEggsSinceStart()
            eggsFoundFormatList.add("§7You found §b$foundMealEggs§7/§a$spawnedEggs §6Chocolate Meal Egg${if (foundMealEggs > 1) "s" else ""}§7.")
        }
        mealTypeMap[HoppityEggType.SIDE_DISH]?.let {
            eggsFoundFormatList.add("§7You found §b$it §6§lSide Dish §r§6Egg${if (it > 1) "s" else ""}§7 §7in the §dChocolate Factory§7.")
        }
        mealTypeMap[HoppityEggType.BOUGHT]?.let {
            eggsFoundFormatList.add("§7You bought §b$it §fRabbit${if (it > 1) "s" else ""} §7from §aHoppity§7.")
        }

        if (eggsFoundFormatList.isEmpty()) {
            eggsFoundFormatList.add("§cNo Chocolate Eggs or Rabbits found during this event§7.")
        }
        return eggsFoundFormatList
    }

    private fun getMealEggsSinceStart(): Int {
        if (!HoppityAPI.isHoppityEvent()) return 0

        val sbTimeNow = SkyBlockTime.now()
        val milliDifference = sbTimeNow.toMillis() - SkyBlockTime.fromSbYear(sbTimeNow.year).toMillis()

        // Calculate total eggs from complete days and incomplete day periods
        var spawnedMealsEggs = (milliDifference / SKYBLOCK_DAY_MILLIS).toInt() * 3

        // Add eggs for the current day based on time of day
        spawnedMealsEggs += when {
            milliDifference % SKYBLOCK_DAY_MILLIS >= SKYBLOCK_HOUR_MILLIS * 21 -> 3 // Dinner egg, 9 PM
            milliDifference % SKYBLOCK_DAY_MILLIS >= SKYBLOCK_HOUR_MILLIS * 14 -> 2 // Lunch egg, 2 PM
            milliDifference % SKYBLOCK_DAY_MILLIS >= SKYBLOCK_HOUR_MILLIS * 7 -> 1 // Breakfast egg, 7 AM
            else -> 0
        }

        return spawnedMealsEggs
    }

    private fun getRabbitsFormat(rarityMap: Map<HoppityRabbitRarity, Int>, name: String, colorCode: String): List<String> {
        val formats = mutableListOf<String>()
        val rabbitsFound = rarityMap.toMutableMap()
        val rabbitsSum = rabbitsFound.sumAllValues().toInt()
        if (rabbitsSum == 0) return formats

        formats.add("§7$name Rabbits: $colorCode$rabbitsSum")

        var addSeparator = false
        val uniqueBuilder = StringBuilder()
        HoppityRabbitRarity.entries.forEach {
            if (addSeparator) uniqueBuilder.append(" §7-") else addSeparator = true
            uniqueBuilder.append(" ${it.colorCode}${rabbitsFound[it] ?: 0}")
        }

        formats.add(uniqueBuilder.toString())

        return formats
    }
}
