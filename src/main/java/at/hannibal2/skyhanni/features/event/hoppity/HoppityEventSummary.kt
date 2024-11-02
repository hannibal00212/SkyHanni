package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.event.hoppity.HoppityEventSummaryConfig.HoppityStat
import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage.HoppityEventStats
import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage.HoppityEventStats.RabbitData
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
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.SkyBlockTime
import at.hannibal2.skyhanni.utils.SkyBlockTime.Companion.SKYBLOCK_DAY_MILLIS
import at.hannibal2.skyhanni.utils.SkyBlockTime.Companion.SKYBLOCK_HOUR_MILLIS
import at.hannibal2.skyhanni.utils.SkyblockSeason
import at.hannibal2.skyhanni.utils.StringUtils
import at.hannibal2.skyhanni.utils.TimeUtils.format
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.milliseconds

@SkyHanniModule
object HoppityEventSummary {
    private val config get() = SkyHanniMod.feature.event.hoppityEggs
    private val lineHeader = " ".repeat(4)

    private var firstInCf = 0L
    private var inCfNow = false

    @HandleEvent
    fun onRabbitFound(event: RabbitFoundEvent) {
        if (!HoppityAPI.isHoppityEvent()) return
        val stats = getYearStats().first ?: return

        stats.mealsFound.addOrPut(event.eggType, 1)
        val rarity = HoppityAPI.rarityByRabbit(event.rabbitName) ?: return
        val rarityMap = stats.rabbitsFound.getOrPut(rarity) { RabbitData() }
        if (event.duplicate) rarityMap.dupes++
        else rarityMap.uniques++
        if (event.chocGained > 0) stats.dupeChocolateGained += event.chocGained
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(64, "event.hoppity.preventMissingFish", "event.hoppity.preventMissingRabbitTheFish")
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!LorenzUtils.inSkyBlock) return
        checkEnded()
        if (!HoppityAPI.isHoppityEvent()) return
        checkInit()
        checkAddCfTime()
    }

    @SubscribeEvent
    fun onProfileJoin(event: ProfileJoinEvent) {
        checkEnded()
    }

    private fun checkInit() {
        val statStorage = ProfileStorageData.profileSpecific?.hoppityEventStats ?: return
        val currentYear = SkyBlockTime.now().year
        if (statStorage.containsKey(currentYear)) return
        statStorage[currentYear] = HoppityEventStats()
    }

    private fun getYearStats(year: Int? = null): Pair<HoppityEventStats?, Int> {
        val queryYear = year ?: SkyBlockTime.now().year
        val storage = ProfileStorageData.profileSpecific?.hoppityEventStats ?: return Pair(null, queryYear)
        if (!storage.containsKey(queryYear)) return Pair(null, queryYear)
        return Pair(storage[queryYear], queryYear)
    }

    private fun checkAddCfTime() {
        if (ChocolateFactoryAPI.inChocolateFactory && !this.inCfNow) {
            this.inCfNow = true
            firstInCf = SkyBlockTime.now().toMillis()
        } else if (!ChocolateFactoryAPI.inChocolateFactory && this.inCfNow) {
            val stats = getYearStats().first ?: return
            stats.millisInCf += (SkyBlockTime.now().toMillis() - firstInCf)
            this.inCfNow = false
            firstInCf = 0
        }
    }

    private fun checkEnded() {
        val (stats, year) = getYearStats()
        if (stats == null || stats.summarized) return

        val currentYear = SkyBlockTime.now().year
        val currentSeason = SkyblockSeason.currentSeason
        val isSpring = currentSeason == SkyblockSeason.SPRING

        if (year < currentYear || (year == currentYear && !isSpring) && config.eventSummary.enabled) {
            sendStatsMessage(stats, year)
            ProfileStorageData.profileSpecific?.hoppityEventStats?.get(year)?.also { it.summarized = true }
                ?: ErrorManager.skyHanniError("Could not save summarization state in Hoppity Event Summarization.")
        }
    }

    // First event was year 346 -> #1, 20th event was year 365, etc.
    private fun getHoppityEventNumber(skyblockYear: Int): Int = (skyblockYear - 345)

    fun addStrayCaught(rarity: LorenzRarity, chocGained: Long) {
        if (!HoppityAPI.isHoppityEvent()) return
        val stats = getYearStats().first ?: return
        val rarityMap = stats.rabbitsFound.getOrPut(rarity) { RabbitData() }
        rarityMap.strays++
        stats.strayChocolateGained += chocGained
    }

    private fun StringBuilder.appendHeadedLine(line: String) {
        appendLine("$lineHeader$line")
    }

    private fun StringBuilder.addExtraChocFormatLine(chocGained: Long) {
        if (chocGained <= 0) return
        appendHeadedLine(
            buildString {
                append(" §6+${chocGained.addSeparators()} Chocolate")
                if (SkyHanniMod.feature.inventory.chocolateFactory.showDuplicateTime) {
                    val timeFormatted = ChocolateFactoryAPI.timeUntilNeed(chocGained).format(maxUnits = 2)
                    append(" §7(§a+§b$timeFormatted§7)")
                }
            },
        )
    }

    private fun HoppityEventStats.getMilestoneCount(): Int =
        (mealsFound[HoppityEggType.CHOCOLATE_FACTORY_MILESTONE] ?: 0) +
            (mealsFound[HoppityEggType.CHOCOLATE_SHOP_MILESTONE] ?: 0)

    private val summaryOperationList by lazy {
        buildMap<HoppityStat, (sb: StringBuilder, stats: HoppityEventStats, year: Int) -> Unit> {
            put(HoppityStat.MEAL_EGGS_FOUND) { sb, stats, year ->
                stats.getEggsFoundFormat(year).takeIf { it != null }?.let {
                    sb.appendHeadedLine(it)
                }
            }

            put(HoppityStat.HOPPITY_RABBITS_BOUGHT) { sb, stats, _ ->
                stats.mealsFound[HoppityEggType.BOUGHT]?.let {
                    sb.appendHeadedLine("§7You bought §b$it §f${StringUtils.pluralize(it, "Rabbit")} §7from §aHoppity§7.")
                }
            }

            put(HoppityStat.SIDE_DISH_EGGS) { sb, stats, _ ->
                stats.mealsFound[HoppityEggType.SIDE_DISH]?.let {
                    sb.appendHeadedLine(
                        "§7You found §b$it §6§lSide Dish §r§6${StringUtils.pluralize(it, "Egg")}§7 " +
                            "§7in the §6Chocolate Factory§7.",
                    )
                }
            }

            put(HoppityStat.MILESTONE_RABBITS) { sb, stats, _ ->
                stats.getMilestoneCount().takeIf { it > 0 }?.let {
                    sb.appendHeadedLine("§7You claimed §b$it §6§lMilestone §r§6${StringUtils.pluralize(it, "Rabbit")}§7.")
                }
            }

            put(HoppityStat.NEW_RABBITS) { sb, stats, _ ->
                getRabbitsFormat(stats.rabbitsFound.mapValues { m -> m.value.uniques }, "Unique").forEach {
                    sb.appendHeadedLine(it)
                }
            }

            put(HoppityStat.DUPLICATE_RABBITS) { sb, stats, _ ->
                getRabbitsFormat(stats.rabbitsFound.mapValues { m -> m.value.dupes }, "Duplicate").forEach {
                    sb.appendHeadedLine(it)
                }
                sb.addExtraChocFormatLine(stats.dupeChocolateGained)
            }

            put(HoppityStat.STRAY_RABBITS) { sb, stats, _ ->
                getRabbitsFormat(stats.rabbitsFound.mapValues { m -> m.value.strays }, "Stray").forEach {
                    sb.appendHeadedLine(it)
                }
                sb.addExtraChocFormatLine(stats.strayChocolateGained)
            }

            put(HoppityStat.TIME_IN_CF) { sb, stats, _ ->
                sb.appendHeadedLine("§7You spent §b${stats.millisInCf.milliseconds.format(maxUnits = 2)} §7in the §6Chocolate Factory§7.")
            }

            put(HoppityStat.EMPTY_1) { sb, _, _ -> sb.appendLine() }
            put(HoppityStat.EMPTY_2) { sb, _, _ -> sb.appendLine() }
            put(HoppityStat.EMPTY_3) { sb, _, _ -> sb.appendLine() }
            put(HoppityStat.EMPTY_4) { sb, _, _ -> sb.appendLine() }
        }
    }

    fun sendStatsMessage(it: Array<String>) {
        val statsStorage = ProfileStorageData.profileSpecific?.hoppityEventStats ?: return
        val currentYear = SkyBlockTime.now().year
        val statsYearList = statsStorage.keys.takeIf { it.isNotEmpty() } ?: mutableListOf()
        val statsYearFormatList = statsStorage.keys.takeIf { it.isNotEmpty() }?.map {
            "§b$it${if (it == currentYear && HoppityAPI.isHoppityEvent()) " §a(Current)§r" else ""}"
        }?.toMutableList() ?: mutableListOf()

        val parsedInt: Int? = if (it.size == 1) it[0].toIntOrNull() else null

        val availableYearsFormat =
            "§eHoppity Event Stats are available for the following years:§r\n${statsYearFormatList.joinToString("§e, ") { it }}"

        if (parsedInt == null) {
            if (HoppityAPI.isHoppityEvent()) {
                val stats = getYearStats(currentYear).first ?: return
                sendStatsMessage(stats, currentYear)
            } else ChatUtils.chat(availableYearsFormat)
        } else if (!statsYearList.contains(parsedInt)) {
            ChatUtils.chat("Could not find data for year §b$parsedInt§e.\n$availableYearsFormat")
        } else {
            val stats = getYearStats(parsedInt).first ?: return
            sendStatsMessage(stats, parsedInt)
        }
    }

    private fun sendStatsMessage(stats: HoppityEventStats, eventYear: Int?) {
        if (eventYear == null) return
        val summaryBuilder: StringBuilder = StringBuilder()
        summaryBuilder.appendLine("§d§l${"▬".repeat(64)}")

        // Header
        summaryBuilder.appendLine("${" ".repeat(26)}§d§lHoppity's Hunt #${getHoppityEventNumber(eventYear)} Stats")
        summaryBuilder.appendLine()

        // Various stats from config
        val statsBuilder: StringBuilder = StringBuilder()
        config.eventSummary.statDisplayList.forEach {
            summaryOperationList[it]?.invoke(statsBuilder, stats, eventYear)
        }

        // If no stats are found, display a message
        if (statsBuilder.toString().replace("\n", "").isEmpty()) {
            statsBuilder.appendHeadedLine("§c§lNothing to show!")
            statsBuilder.appendHeadedLine("§c§oGo find some eggs!")
        }

        // Remove any consecutive empty lines in the stats
        val statsBuilderString = statsBuilder.toString().replace(Regex("\n{4,}"), "\n".repeat(3))

        // Append stats
        summaryBuilder.append(statsBuilderString)

        // Footer
        summaryBuilder.append("§d§l${"▬".repeat(64)}")

        ChatUtils.chat(summaryBuilder.toString(), prefix = false)
    }

    private fun HoppityEventStats.getEggsFoundFormat(year: Int): String? =
        mealsFound.filterKeys { it in HoppityEggType.resettingEntries }.sumAllValues().toInt().takeIf { it > 0 }?.let {
            val milliDifference = SkyBlockTime.now().toMillis() - SkyBlockTime.fromSbYear(year).toMillis()
            val pastEvent = milliDifference > SkyBlockTime.SKYBLOCK_SEASON_MILLIS
            // Calculate total eggs from complete days and incomplete day periods
            val previousEggs = if (pastEvent) 279 else (milliDifference / SKYBLOCK_DAY_MILLIS).toInt() * 3
            val currentEggs = when {
                pastEvent -> 0
                // Add eggs for the current day based on time of day
                milliDifference % SKYBLOCK_DAY_MILLIS >= SKYBLOCK_HOUR_MILLIS * 21 -> 3 // Dinner egg, 9 PM
                milliDifference % SKYBLOCK_DAY_MILLIS >= SKYBLOCK_HOUR_MILLIS * 14 -> 2 // Lunch egg, 2 PM
                milliDifference % SKYBLOCK_DAY_MILLIS >= SKYBLOCK_HOUR_MILLIS * 7 -> 1 // Breakfast egg, 7 AM
                else -> 0
            }
            val spawnedMealsEggs = previousEggs + currentEggs
            "§7You found §b$it§7/§a$spawnedMealsEggs §6Chocolate Meal ${StringUtils.pluralize(it, "Egg")}§7."
        }


    private fun getRabbitsFormat(rarityMap: Map<LorenzRarity, Int>, name: String): List<String> {
        val rabbitsSum = rarityMap.values.sum()
        if (rabbitsSum == 0) return emptyList()

        return mutableListOf(
            "§7$name Rabbits: §f$rabbitsSum",
            HoppityAPI.hoppityRarities.joinToString(" §7-") {
                " ${it.chatColorCode}${rarityMap[it] ?: 0}"
            },
        )
    }
}
