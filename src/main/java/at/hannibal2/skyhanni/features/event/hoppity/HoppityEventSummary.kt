package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.features.event.hoppity.HoppityEventSummaryConfig.HoppityStat
import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage.HoppityEventStats
import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage.HoppityEventStats.LeaderboardPosition
import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage.HoppityEventStats.RabbitData
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzKeyPressEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.hoppity.RabbitFoundEvent
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.CollectionUtils.sumAllValues
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockTime
import at.hannibal2.skyhanni.utils.SkyBlockTime.Companion.SKYBLOCK_DAY_MILLIS
import at.hannibal2.skyhanni.utils.SkyBlockTime.Companion.SKYBLOCK_HOUR_MILLIS
import at.hannibal2.skyhanni.utils.SkyblockSeason
import at.hannibal2.skyhanni.utils.StringUtils
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard
import kotlin.time.Duration.Companion.milliseconds

@SkyHanniModule
object HoppityEventSummary {
    /**
     * REGEX-TEST: §d§lHOPPITY'S HUNT §r§7You found §r§cRabbit the Fish§r§7!
     */
    private val rabbitTheFishPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "rabbit.thefish",
        "(?:§.)*HOPPITY'S HUNT (?:§.)*You found (?:§.)*Rabbit the Fish§(?:§.)*!.*"
    )

    private val config get() = SkyHanniMod.feature.event.hoppityEggs
    private val liveDisplayConfig get() = config.eventSummary.liveDisplay
    private val lineHeader = " ".repeat(4)

    private var displayCardRenderables = listOf<Renderable>()
    private var lastKnownStatHash = 0
    private var lastKnownInInvState = false
    private var lastAddedCfMillis: SimpleTimeMark? = null

    private data class StatString(val string: String, val headed: Boolean = true)

    private val EGGLOCATOR_ITEM = "EGG_LOCATOR".asInternalName()
    private fun isEggLocatorOverridden(): Boolean =
        liveDisplayConfig.showHoldingEgglocator && InventoryUtils.itemInHandId == EGGLOCATOR_ITEM
    private fun liveDisplayEnabled(): Boolean {
        val profileStorage = ProfileStorageData.profileSpecific ?: return false
        val isEnabled = liveDisplayConfig.enabled
        val isEventEnabled = !liveDisplayConfig.onlyDuringEvent || HoppityAPI.isHoppityEvent()
        val isToggledOff = profileStorage.hoppityStatLiveDisplayToggled

        return LorenzUtils.inSkyBlock && isEnabled && (isEggLocatorOverridden() || (!isToggledOff && isEventEnabled))
    }

    @SubscribeEvent
    fun onKeyPress(event: LorenzKeyPressEvent) {
        if (liveDisplayConfig.enabled) return
        if (liveDisplayConfig.toggleKeybind == Keyboard.KEY_NONE || liveDisplayConfig.toggleKeybind != event.keyCode) return
        val profileStorage = ProfileStorageData.profileSpecific ?: return
        profileStorage.hoppityStatLiveDisplayToggled = !profileStorage.hoppityStatLiveDisplayToggled
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!HoppityAPI.isHoppityEvent()) return
        val stats = getYearStats().first ?: return

        if (rabbitTheFishPattern.matches(event.message)) {
            stats.rabbitTheFishFinds++
        }
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent) {
        if (!liveDisplayEnabled()) return
        val profileStorage = ProfileStorageData.profileSpecific ?: return
        val statYear = profileStorage.hoppityStatLiveDisplayYear.takeIf { it != -1 }
            ?: SkyBlockTime.now().year

        val stats = getYearStats(statYear).first
        // Calculate a 'hash' of the stats to determine if they have changed
        val statsHash = stats?.hashCode() ?: 0
        if (statsHash != lastKnownStatHash) {
            lastKnownStatHash = statsHash
            displayCardRenderables = buildDisplayRenderables(stats, statYear)
        }

        config.eventSummary.liveDisplayPosition.renderRenderables(
            displayCardRenderables,
            posLabel = "Hoppity's Hunt Stats",
        )
    }

    private fun isInInventory(): Boolean =
        Minecraft.getMinecraft().currentScreen is GuiInventory ||
            Minecraft.getMinecraft().currentScreen is GuiChest

    private fun buildDisplayRenderables(stats: HoppityEventStats?, statYear: Int): List<Renderable> = buildList {
        // Add title renderable with centered alignment
        add(
            Renderable.string(
                "§dHoppity's Hunt #${getHoppityEventNumber(statYear)} Stats",
                horizontalAlign = RenderUtils.HorizontalAlignment.CENTER
            )
        )

        // Conditionally add year switcher renderable for inventory or chest screens
        if (isInInventory()) {
            buildYearSwitcherRenderables(statYear)?.let { yearSwitcher ->
                add(
                    Renderable.horizontalContainer(
                        yearSwitcher,
                        spacing = 5,
                        horizontalAlign = RenderUtils.HorizontalAlignment.CENTER
                    )
                )
            }
        }

        // Add card renderable based on stats availability
        val cardRenderable = if (stats == null) {
            Renderable.verticalContainer(
                mutableListOf(Renderable.string("§cNo stats found for Hunt #${getHoppityEventNumber(statYear)}."))
            )
        } else {
            Renderable.verticalContainer(
                getStatsStrings(stats, statYear).map { Renderable.string(it.string) }.toMutableList()
            )
        }
        add(cardRenderable)
    }

    private fun buildYearSwitcherRenderables(currentStatYear: Int): List<Renderable>? {
        val profileStorage = ProfileStorageData.profileSpecific ?: return null
        val statsStorage = profileStorage.hoppityEventStats
        val statsYearList = statsStorage.keys.takeIf { it.isNotEmpty() } ?: mutableListOf()

        val predecessorYear = statsYearList.filter { it < currentStatYear }.takeIf { it.any() }?.max()
        val successorYear = statsYearList.filter { it > currentStatYear }.takeIf { it.any() }?.min()
        if (predecessorYear == null && successorYear == null) return null

        return listOfNotNull(
            predecessorYear?.let {
                Renderable.optionalLink(
                    "§d[ §r§f§l<- §r§7Hunt #${getHoppityEventNumber(it)} §r§d]",
                    onClick = { profileStorage.hoppityStatLiveDisplayYear = it }
                )
            },
            successorYear?.let {
                Renderable.optionalLink(
                    "§d[ §7Hunt #${getHoppityEventNumber(it)} §r§f§l-> §r§d]",
                    onClick = { profileStorage.hoppityStatLiveDisplayYear = it }
                )
            }
        )
    }

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
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (lastKnownInInvState != isInInventory()) {
            lastKnownInInvState = !lastKnownInInvState
            lastKnownStatHash = 0
        }
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
        if (!ChocolateFactoryAPI.inChocolateFactory) return
        val stats = getYearStats().first ?: return
        lastAddedCfMillis?.let {
            stats.millisInCf += (SimpleTimeMark.now().toMillis() - it.toMillis())
        }
        lastAddedCfMillis = SimpleTimeMark.now()
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

    fun updateCfPosition(position: Int?, percentile: Double?) {
        if (!HoppityAPI.isHoppityEvent() || position == null || percentile == null) return
        val stats = getYearStats().first ?: return
        val snapshot = LeaderboardPosition(position, percentile)
        stats.initialLeaderboardPosition = stats.initialLeaderboardPosition.takeIf { it.position != -1 } ?: snapshot
        stats.finalLeaderboardPosition = snapshot
    }

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

    private fun MutableList<StatString>.addExtraChocFormatLine(chocGained: Long) {
        if (chocGained <= 0) return
        val chocFormatLine = buildString {
            append(" §6+${chocGained.addSeparators()} Chocolate")
            if (SkyHanniMod.feature.inventory.chocolateFactory.showDuplicateTime) {
                val timeFormatted = ChocolateFactoryAPI.timeUntilNeed(chocGained).format(maxUnits = 2)
                append(" §7(§a+§b$timeFormatted§7)")
            }
        }
        add(StatString(chocFormatLine))
    }

    private fun HoppityEventStats.getMilestoneCount(): Int =
        (mealsFound[HoppityEggType.CHOCOLATE_FACTORY_MILESTONE] ?: 0) +
            (mealsFound[HoppityEggType.CHOCOLATE_SHOP_MILESTONE] ?: 0)

    private val summaryOperationList by lazy {
        buildMap<HoppityStat, (statList: MutableList<StatString>, stats: HoppityEventStats, year: Int) -> Unit> {
            put(HoppityStat.MEAL_EGGS_FOUND) { sl, stats, year ->
                stats.getEggsFoundFormat(year).takeIf { it != null }?.let {
                    sl.add(StatString(it))
                }
            }

            put(HoppityStat.HOPPITY_RABBITS_BOUGHT) { sl, stats, _ ->
                stats.mealsFound[HoppityEggType.BOUGHT]?.let {
                    sl.add(StatString("§7You bought §b$it §f${StringUtils.pluralize(it, "Rabbit")} §7from §aHoppity§7."))
                }
            }

            put(HoppityStat.SIDE_DISH_EGGS) { sl, stats, _ ->
                stats.mealsFound[HoppityEggType.SIDE_DISH]?.let {
                    sl.add(
                        StatString(
                            "§7You found §b$it §6§lSide Dish ${StringUtils.pluralize(it, "Egg")} " +
                                "§r§7in the §6Chocolate Factory§7.",
                        ),
                    )
                }
            }

            put(HoppityStat.MILESTONE_RABBITS) { sl, stats, _ ->
                stats.getMilestoneCount().takeIf { it > 0 }?.let {
                    sl.add(StatString("§7You claimed §b$it §6§lMilestone §r§6${StringUtils.pluralize(it, "Rabbit")}§7."))
                }
            }

            put(HoppityStat.NEW_RABBITS) { sl, stats, _ ->
                getRabbitsFormat(stats.rabbitsFound.mapValues { m -> m.value.uniques }, "Unique").forEach {
                    sl.add(StatString(it))
                }
            }

            put(HoppityStat.DUPLICATE_RABBITS) { sl, stats, _ ->
                getRabbitsFormat(stats.rabbitsFound.mapValues { m -> m.value.dupes }, "Duplicate").forEach {
                    sl.add(StatString(it))
                }
                sl.addExtraChocFormatLine(stats.dupeChocolateGained)
            }

            put(HoppityStat.STRAY_RABBITS) { sl, stats, _ ->
                getRabbitsFormat(stats.rabbitsFound.mapValues { m -> m.value.strays }, "Stray").forEach {
                    sl.add(StatString(it))
                }
                sl.addExtraChocFormatLine(stats.strayChocolateGained)
            }

            put(HoppityStat.TIME_IN_CF) { sl, stats, _ ->
                val cfTimeFormat = stats.millisInCf.milliseconds.format(maxUnits = 2)
                sl.add(StatString("§7You spent §b$cfTimeFormat §7in the §6Chocolate Factory§7."))
            }

            put(HoppityStat.RABBIT_THE_FISH_FINDS) { sl, stats, _ ->
                stats.rabbitTheFishFinds.takeIf { it > 0 }?.let {
                    val timesFormat = StringUtils.pluralize(it, "time")
                    val eggsFormat = StringUtils.pluralize(it, "Egg")
                    sl.add(StatString("§7You found §cRabbit the Fish §7in Meal $eggsFormat §b$it §7$timesFormat."))
                }
            }

            put(HoppityStat.LEADERBOARD_CHANGE) { sl, stats, _ ->
                val initial = stats.initialLeaderboardPosition
                val final = stats.finalLeaderboardPosition
                if (
                    initial.position == -1 || final.position == -1 ||
                    initial.percentile == -1.0 || final.percentile == -1.0 ||
                    initial.position == final.position
                ) return@put
                getFullLeaderboardMessage(initial, final).forEach {
                    sl.add(StatString(it))
                }
            }

            val emptyStatString = StatString("", false)
            put(HoppityStat.EMPTY_1) { sl, _, _ -> sl.add(emptyStatString) }
            put(HoppityStat.EMPTY_2) { sl, _, _ -> sl.add(emptyStatString) }
            put(HoppityStat.EMPTY_3) { sl, _, _ -> sl.add(emptyStatString) }
            put(HoppityStat.EMPTY_4) { sl, _, _ -> sl.add(emptyStatString) }
        }
    }

    private fun getFullLeaderboardMessage(initial: LeaderboardPosition, final: LeaderboardPosition) = buildList {
        add("§7Leaderboard: ${getPrimaryLbString(initial, final)}")
        add(getSecondaryLbLine(initial, final))
    }

    private fun getPrimaryLbString(initial: LeaderboardPosition, final: LeaderboardPosition): String {
        val iPo = initial.position
        val fPo = final.position
        return "§b#${iPo.addSeparators()} §c-> §b#${fPo.addSeparators()}"
    }

    private fun getSecondaryLbLine(initial: LeaderboardPosition, final: LeaderboardPosition): String {
        val iPo = initial.position
        val fPo = final.position
        val dPo = fPo - iPo
        val iPe = initial.percentile
        val fPe = final.percentile
        val dPe = fPe - iPe
        val preambleFormat = if (iPo > fPo) "§a+" else "§c"

        return buildString {
            append("§7($preambleFormat${(-1 * dPo).addSeparators()} ${StringUtils.pluralize(dPo, "spot")}§7)")
            if (dPe != 0.0) append(" §7Top §a$iPe% §c-> §7Top §a$fPe%")
            else append(" §7Top §a$iPe%")
        }
    }

    private fun getStatsStrings(stats: HoppityEventStats, eventYear: Int?): MutableList<StatString> {
        if (eventYear == null) return mutableListOf()
        val statList = mutableListOf<StatString>()

        // Various stats from config
        config.eventSummary.statDisplayList.forEach {
            summaryOperationList[it]?.invoke(statList, stats, eventYear)
        }

        // Remove any consecutive empty lines
        val iterator = statList.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next.string.isEmpty() && iterator.hasNext()) {
                val nextNext = iterator.next()
                if (nextNext.string.isEmpty()) iterator.remove()
            }
        }

        return statList
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
        getStatsStrings(stats, eventYear).forEach {
            if (it.headed) statsBuilder.appendHeadedLine(it.string)
            else statsBuilder.appendLine(it.string)
        }

        // If no stats are found, display a message
        if (statsBuilder.toString().replace("\n", "").isEmpty()) {
            statsBuilder.appendHeadedLine("§c§lNothing to show!")
            statsBuilder.appendHeadedLine("§c§oGo find some eggs!")
        }

        // Append stats
        summaryBuilder.append(statsBuilder)

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
