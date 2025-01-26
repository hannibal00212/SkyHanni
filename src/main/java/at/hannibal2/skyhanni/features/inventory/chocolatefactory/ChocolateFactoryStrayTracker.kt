package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.hoppity.EggFoundEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityApi
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEventSummary
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryApi.partyModeReplace
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.CollectionUtils.sortedDesc
import at.hannibal2.skyhanni.utils.ConditionalUtils.onToggle
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getSingleLineLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.LorenzRarity.LEGENDARY
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils
import at.hannibal2.skyhanni.utils.StringUtils.removeResets
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.Searchable
import at.hannibal2.skyhanni.utils.renderables.toSearchable
import at.hannibal2.skyhanni.utils.tracker.SkyHanniTracker
import at.hannibal2.skyhanni.utils.tracker.TrackerData
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import net.minecraft.item.ItemStack
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object ChocolateFactoryStrayTracker {

    private val config get() = ChocolateFactoryApi.config
    private val claimedStraysSlots = mutableListOf<Int>()

    // <editor-fold desc="Patterns">
    /**
     * REGEX-TEST: §9Zero §d§lCAUGHT!
     * REGEX-TEST: §6§lGolden Rabbit §d§lCAUGHT!
     * REGEX-TEST: §fAudi §d§lCAUGHT!
     */
    val strayCaughtPattern by ChocolateFactoryApi.patternGroup.pattern(
        "stray.caught",
        "^(?:§.)*(?<name>.*) §d§lCAUGHT!",
    )

    /**
     * REGEX-TEST: §7You caught a stray §fMandy §7and §7gained §6+283,574 Chocolate§7!
     * REGEX-TEST: §7You caught a stray §aSven §7and gained §7§6+397,004 Chocolate§7!
     */
    private val strayLorePattern by ChocolateFactoryApi.patternGroup.pattern(
        "stray.loreinfo",
        "§7You caught a stray (?<rabbit>(?<name>§[^§]*)) .* (?:§7)?§6\\+(?<amount>[\\d,]*) Chocolate§7!",
    )

    /**
     * REGEX-TEST: §7You caught a stray §6§lGolden Rabbit§7! §7You gained §6+13,566,571 Chocolate§7!
     */
    private val goldenStrayJackpotMountainPattern by ChocolateFactoryApi.patternGroup.pattern(
        "stray.goldenrawchoc",
        "§7You caught a stray §6§lGolden Rabbit§7! §7You gained §6\\+(?<amount>[\\d,]*) Chocolate§7!",
    )

    /**
     * REGEX-TEST: §7You caught a stray §6§lGolden Rabbit§7! §7You caught a glimpse of §6El Dorado§7, §7but he escaped and left behind §7§6313,780 Chocolate§7!
     * REGEX-TEST: §7You caught a stray §6§lGolden Rabbit§7! §7You caught §6El Dorado §7- quite the elusive rabbit!
     * REGEX-TEST: §7You caught a stray §6§lGolden Rabbit§7! §7You caught §6El Dorado§7! Since you §7already have captured him before, §7you gained §6+324,364,585 Chocolate§7.
     */
    val strayDoradoPattern by ChocolateFactoryApi.patternGroup.pattern(
        "stray.dorado",
        ".*§7You caught a stray.*§6El Dorado(?:.*?§6\\+?(?<amount>[\\d,]+) Chocolate)?.*",
    )

    /**
     * REGEX-TEST: §7You caught a stray §6§lGolden Rabbit§7! §7A hoard of §aStray Rabbits §7has §7appeared!
     */
    private val strayHoardPattern by ChocolateFactoryApi.patternGroup.pattern(
        "stray.hoard",
        ".*(?:§r)?§7A hoard of (?:§r)?§aStray Rabbits (?:§r)?§7has.*",
    )

    /**
     * REGEX-TEST: §7You caught a stray §6§lGolden Rabbit§7! §7You gained §6+5 Chocolate §7until the §7end of the SkyBlock year!
     */
    private val goldenStrayClick by ChocolateFactoryApi.patternGroup.pattern(
        "stray.goldenclick",
        "§7You caught a stray §6§lGolden Rabbit§7! §7You gained §6\\+5 Chocolate §7until the §7end of the SkyBlock year!",
    )

    /**
     * REGEX-TEST: §7You caught a stray §9Fish the Rabbit§7! §7You have already found §9Fish the §9Rabbit§7, so you received §655,935,257 §6Chocolate§7!
     * REGEX-TEST: §7You caught a stray §9Fish the Rabbit§7!
     */
    @Suppress("MaxLineLength")
    private val fishTheRabbitPattern by ChocolateFactoryApi.patternGroup.pattern(
        "stray.fish",
        "§7You caught a stray (?<color>§.)Fish the Rabbit§7!(?: §7You have already found (?:§.)?Fish the (?:§.)?Rabbit§7, so you received §6(?<amount>[\\d,]*) (?:§6)?Chocolate§7!|.*)?",
    )

    /**
     * REGEX-TEST: §7You have already found §9Fish the
     */
    val duplicatePseudoStrayPattern by ChocolateFactoryApi.patternGroup.pattern(
        "stray.pseudoduplicate",
        "(?:§.)*You have already found.*",
    )

    /**
     * REGEX-TEST: §7already have captured him before
     */
    val duplicateDoradoStrayPattern by ChocolateFactoryApi.patternGroup.pattern(
        "stray.doradoduplicate",
        "(?:§.)*already have captured him before.*",
    )

    /**
     * REGEX-TEST: §7but he escaped and left behind
     * REGEX-TEST: §7§6Legend of §6El Dorado §7grows!
     */
    val doradoEscapeStrayPattern by ChocolateFactoryApi.patternGroup.pattern(
        "stray.doradoescape",
        ".*(?:§.)*(?:but he escaped and left behind|Legend of (?:§.)*El Dorado (?:§.)*grows!).*",
    )
    // </editor-fold>

    private val tracker = SkyHanniTracker("Stray Tracker", { Data() }, { it.chocolateFactory.strayTracker }) {
        drawDisplay(it)
    }

    class Data : TrackerData() {
        override fun reset() {
            straysCaught.clear()
            straysExtraChocMs.clear()
            goldenTypesCaught.clear()
        }

        @Expose
        var straysCaught: MutableMap<LorenzRarity, Int> = mutableMapOf()

        @Expose
        var straysExtraChocMs: MutableMap<LorenzRarity, Long> = mutableMapOf()

        @Expose
        var goldenTypesCaught: MutableMap<String, Int> = mutableMapOf()
    }

    private fun incrementRarity(rarity: LorenzRarity, chocAmount: Long = 0) {
        tracker.modify { it.straysCaught.addOrPut(rarity, 1) }
        val extraTime = ChocolateFactoryApi.timeUntilNeed(chocAmount + 1)
        tracker.modify { it.straysExtraChocMs.addOrPut(rarity, extraTime.inWholeMilliseconds) }
        if (!HoppityApi.isHoppityEvent()) return
        HoppityEventSummary.addStrayCaught(rarity, chocAmount)
    }

    private fun incrementGoldenType(typeCaught: String, amount: Int = 1) {
        tracker.modify { it.goldenTypesCaught.addOrPut(typeCaught, amount) }
    }

    private fun drawDisplay(data: Data): List<Searchable> = buildList {
        val extraChocMs = data.straysExtraChocMs.values.sum().milliseconds
        val formattedExtraTime = extraChocMs.let { if (it == 0.milliseconds) "0s" else it.format() }

        add(
            Renderable.hoverTips(
                "§6§lStray Tracker".partyModeReplace(),
                tips = listOf("§a+§b$formattedExtraTime §afrom strays§7".partyModeReplace()),
            ).toSearchable(),
        )
        HoppityApi.hoppityRarities.forEach { rarity ->
            extractHoverableOfRarity(rarity, data)?.let { add(it) }
        }
    }

    private fun extractHoverableOfRarity(rarity: LorenzRarity, data: Data): Searchable? {
        val caughtOfRarity = data.straysCaught[rarity]
        val caughtString = caughtOfRarity?.toString() ?: return null

        val rarityExtraChocMs = data.straysExtraChocMs[rarity]?.milliseconds
        val extraChocFormat = rarityExtraChocMs?.format().orEmpty()

        val colorCode = rarity.chatColorCode
        val lineHeader = "$colorCode${rarity.toString().lowercase().replaceFirstChar { it.uppercase() }}§7: §r$colorCode"
        val lineFormat = "$lineHeader$caughtString".partyModeReplace()

        val renderable = rarityExtraChocMs?.let {
            var tip = "§a+§b$extraChocFormat §afrom $colorCode${rarity.toString().lowercase()} strays§7"
            if (rarity == LEGENDARY) tip += extractGoldenTypesCaught(data)
            Renderable.hoverTips(Renderable.string(lineFormat), tips = tip.partyModeReplace().split("\n"))
        } ?: Renderable.string(lineFormat)
        return renderable.toSearchable(rarity.toString())
    }

    private val goldenTypesMap: Map<String, (Int, MutableList<String>) -> Unit> by lazy {
        mapOf(
            "sidedish" to { count, list -> list.add("§b$count §6Side ${StringUtils.pluralize(count, "Dish", "Dishes")}") },
            "jackpot" to { count, list -> list.add("§b$count §6Chocolate ${StringUtils.pluralize(count, "Jackpot")}") },
            "mountain" to { count, list -> list.add("§b$count §6Chocolate ${StringUtils.pluralize(count, "Mountain")}") },
            "dorado" to { count, list -> list.add("§b$count §6El Dorado ${StringUtils.pluralize(count, "Sighting")}") },
            "stampede" to { count, list -> list.add("§b$count §6${StringUtils.pluralize(count, "Stampede")}") },
            "goldenclick" to { count, list -> list.add("§b$count §6Golden ${StringUtils.pluralize(count, "Click")}") },
        )
    }

    private fun extractGoldenTypesCaught(data: Data): String {
        val goldenList = mutableListOf<String>()
        data.goldenTypesCaught.sortedDesc().forEach { (key, count) -> goldenTypesMap[key]?.invoke(count, goldenList) }
        return if (goldenList.isEmpty()) "" else ("\n" + goldenList.joinToString("\n"))
    }

    fun handleStrayClicked(slotNumber: Int, itemStack: ItemStack): Boolean {
        if (!isEnabled() || claimedStraysSlots.contains(slotNumber)) return false

        claimedStraysSlots.add(slotNumber)
        val loreLine = itemStack.getSingleLineLore()

        // "Base" strays - Common -> Epic, raw choc only reward.
        strayLorePattern.matchMatcher(loreLine) {
            // Pretty sure base strays max at Legendary, but...
            val rarity = HoppityApi.rarityByRabbit(group("rabbit")) ?: return@matchMatcher
            incrementRarity(rarity, group("amount").formatLong())
        }

        // Fish the Rabbit
        fishTheRabbitPattern.matchMatcher(loreLine) {
            // Also fairly sure that Fish maxes out at Rare, but...
            val rarity = HoppityApi.rarityByRabbit(group("color")) ?: return@matchMatcher
            groupOrNull("amount")?.let { amount ->
                incrementRarity(rarity, amount.formatLong())
            } ?: incrementRarity(rarity)
        }

        // Golden Strays, Jackpot and Mountain, raw choc only reward.
        goldenStrayJackpotMountainPattern.matchMatcher(loreLine) {
            val amount = group("amount").formatLong().also { am -> incrementRarity(LEGENDARY, am) }
            val multiplier = amount / ChocolateFactoryApi.chocolatePerSecond
            when (multiplier) {
                in 479.0..481.0 -> incrementGoldenType("jackpot")
                in 1499.0..1501.0 -> incrementGoldenType("mountain")
            }
        }

        // Golden Strays, "Golden Click"
        goldenStrayClick.matchMatcher(loreLine) {
            incrementGoldenType("goldenclick")
            incrementRarity(LEGENDARY, 0)
        }

        // Golden Strays, hoard/stampede
        strayHoardPattern.matchMatcher(loreLine.removeResets()) {
            incrementGoldenType("stampede")
            incrementRarity(LEGENDARY, 0)
        }

        // El Dorado - all catches
        strayDoradoPattern.matchMatcher(loreLine) {
            groupOrNull("amount")?.let { amount ->
                incrementRarity(LEGENDARY, amount.formatLong())
            }
            incrementGoldenType("dorado")
        }

        return true
    }

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return
        InventoryUtils.getItemsInOpenChest().filter {
            claimedStraysSlots.contains(it.slotIndex)
        }.forEach {
            if (!strayCaughtPattern.matches(it.stack.name)) {
                claimedStraysSlots.removeAt(claimedStraysSlots.indexOf(it.slotIndex))
            }
        }
    }

    @HandleEvent
    fun onEggFound(event: EggFoundEvent) {
        if (!isEnabled() || event.type != HoppityEggType.SIDE_DISH) return
        event.slotIndex?.let {
            claimedStraysSlots.add(it)
            DelayedRun.runDelayed(1.seconds) {
                claimedStraysSlots.remove(claimedStraysSlots.indexOf(it))
            }
        }
        incrementRarity(LEGENDARY, 0)
        incrementGoldenType("sidedish")
    }

    init {
        tracker.initRenderer(config.strayRabbitTrackerPosition) { isEnabled() }
    }

    @HandleEvent
    fun onInventoryFullyOpened(event: InventoryFullyOpenedEvent) {
        if (!isEnabled()) return
        // Force a refresh for party mode
        if (ChocolateFactoryApi.inChocolateFactory && config.partyMode.get()) tracker.update()
        tracker.firstUpdate()
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (!isEnabled()) return
        tracker.update() // Make sure we don't stay in party mode
    }

    private fun <T> migrateJsonStringKeyToRarityKey(jElement: JsonElement, enumClass: Class<T>): JsonElement {
        if (!jElement.isJsonObject) return jElement
        val newElement = JsonObject()

        for ((key, value) in jElement.asJsonObject.entrySet()) {
            val enum = try {
                enumClass.javaClass.enumConstants.first { it.name.equals(key, ignoreCase = true) }
            } catch (e: IllegalArgumentException) {
                continue
            }
            value?.asInt?.let { newElement.addProperty(enum.toString(), it) }
        }

        return newElement
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.transform(58, "chocolateFactory.strayTracker.straysCaught") { element ->
            migrateJsonStringKeyToRarityKey(element, LorenzRarity::class.java)
        }
        event.transform(58, "chocolateFactory.strayTracker.straysExtraChocMs") { element ->
            migrateJsonStringKeyToRarityKey(element, LorenzRarity::class.java)
        }
    }

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        config.partyMode.onToggle(tracker::update)
    }

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("shresetstrayrabbittracker") {
            description = "Resets the Stray Rabbit Tracker"
            category = CommandCategory.USERS_RESET
            callback { tracker.resetCommand() }
        }
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.strayRabbitTracker && ChocolateFactoryApi.inChocolateFactory
}
