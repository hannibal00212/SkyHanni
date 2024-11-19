package at.hannibal2.skyhanni.features.garden.visitor

import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.garden.visitor.DropsStatisticsConfig.DropsStatisticsTextEntry
import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.garden.visitor.VisitorAcceptEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.addAsSingletonList
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.CollectionUtils.editCopy
import at.hannibal2.skyhanni.utils.ConfigUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.renderStringsAndItems
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object GardenVisitorDropStatistics {

    private val config get() = GardenAPI.config.visitors.dropsStatistics
    private var display = emptyList<List<Any>>()

    private var acceptedVisitors = 0
    var deniedVisitors = 0
    private var totalVisitors = 0
    var coinsSpent = 0L

    var lastAccept = SimpleTimeMark.farPast()

    private val patternGroup = RepoPattern.group("garden.visitor.droptracker")

    /**
     * REGEX-TEST: OFFER ACCEPTED with Duke (UNCOMMON)
     */
    private val acceptPattern by patternGroup.pattern(
        "accept",
        "OFFER ACCEPTED with (?<visitor>.*) [(](?<rarity>.*)[)]",
    )

    /**
     * REGEX-TEST: +20 Copper
     */
    private val copperPattern by patternGroup.pattern(
        "copper",
        "[+](?<amount>.*) Copper",
    )

    /**
     * REGEX-TEST: +20 Garden Experience
     */
    private val gardenExpPattern by patternGroup.pattern(
        "gardenexp",
        "[+](?<amount>.*) Garden Experience",
    )

    /**
     * REGEX-TEST: +18.2k Farming XP
     */
    private val farmingExpPattern by patternGroup.pattern(
        "farmingexp",
        "[+](?<amount>.*) Farming XP",
    )

    /**
     * REGEX-TEST: +12 Bits
     */
    private val bitsPattern by patternGroup.pattern(
        "bits",
        "[+](?<amount>.*) Bits",
    )

    /**
     * REGEX-TEST: +968 Mithril Powder
     */
    private val mithrilPowderPattern by patternGroup.pattern(
        "powder.mithril",
        "[+](?<amount>.*) Mithril Powder",
    )

    /**
     * REGEX-TEST: +754 Gemstone Powder
     */
    private val gemstonePowderPattern by patternGroup.pattern(
        "powder.gemstone",
        "[+](?<amount>.*) Gemstone Powder",
    )

    private var rewardsCount = mapOf<VisitorReward, Int>()

    private fun formatDisplay(map: List<List<Any>>): List<List<Any>> {
        val newList = mutableListOf<List<Any>>()
        for (index in config.textFormat) {
            // We need to use the ordinal here, can't change this.
            newList.add(map[index.ordinal])
        }
        return newList
    }

    @SubscribeEvent
    fun onProfileJoin(event: ProfileJoinEvent) {
        display = emptyList()
    }

    @SubscribeEvent
    fun onVisitorAccept(event: VisitorAcceptEvent) {
        if (!GardenAPI.onBarnPlot) return
        if (!ProfileStorageData.loaded) return

        for (internalName in event.visitor.allRewards) {
            val reward = VisitorReward.getByInternalName(internalName) ?: continue
            rewardsCount = rewardsCount.editCopy { addOrPut(reward, 1) }
            saveAndUpdate()
        }
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!GardenAPI.onBarnPlot) return
        if (!ProfileStorageData.loaded) return
        if (lastAccept.passedSince() > 1.seconds) return

        val message = event.message.removeColor().trim()
        val storage = GardenAPI.storage?.visitorDrops ?: return

        copperPattern.matchMatcher(message) {
            val amount = group("amount").formatInt()
            storage.copper += amount
            saveAndUpdate()
        }
        farmingExpPattern.matchMatcher(message) {
            val amount = group("amount").formatInt()
            storage.farmingExp += amount
            saveAndUpdate()
        }
        gardenExpPattern.matchMatcher(message) {
            val amount = group("amount").formatInt()
            if (amount > 80) return // some of the low visitor milestones will get through but will be minimal
            storage.gardenExp += amount
            saveAndUpdate()
        }
        bitsPattern.matchMatcher(message) {
            val amount = group("amount").formatInt()
            storage.bits += amount
            saveAndUpdate()
        }
        mithrilPowderPattern.matchMatcher(message) {
            val amount = group("amount").formatInt()
            storage.mithrilPowder += amount
            saveAndUpdate()
        }
        gemstonePowderPattern.matchMatcher(message) {
            val amount = group("amount").formatInt()
            storage.gemstonePowder += amount
            saveAndUpdate()
        }
        acceptPattern.matchMatcher(message) {
            setRarities(group("rarity"))
            saveAndUpdate()
        }
    }

    private fun setRarities(rarity: String) {
        acceptedVisitors += 1
        val currentRarity = LorenzUtils.enumValueOf<VisitorRarity>(rarity)
        val visitorRarities = GardenAPI.storage?.visitorDrops?.visitorRarities ?: return
        fixRaritiesSize(visitorRarities)
        // TODO, change functionality to use enum rather than ordinals
        val temp = visitorRarities[currentRarity.ordinal] + 1
        visitorRarities[currentRarity.ordinal] = temp
        saveAndUpdate()
    }

    /**
     * Do not change the order of the elements getting added to the list. See DropsStatisticsTextEntry for the order.
     */
    private fun drawDisplay(storage: ProfileSpecificStorage.GardenStorage.VisitorDrops) = buildList<List<Any>> {
        addAsSingletonList("§e§lVisitor Statistics")
        addAsSingletonList(format(totalVisitors, "Total", "§e", ""))
        val visitorRarities = storage.visitorRarities
        fixRaritiesSize(visitorRarities)
        if (visitorRarities.isNotEmpty()) {
            addAsSingletonList(
                "§a${visitorRarities[0].addSeparators()}§f-" +
                    "§9${visitorRarities[1].addSeparators()}§f-" +
                    "§6${visitorRarities[2].addSeparators()}§f-" +
                    "§d${visitorRarities[3].addSeparators()}§f-" +
                    "§c${visitorRarities[4].addSeparators()}",
            )
        } else {
            addAsSingletonList("§c?")
            ErrorManager.logErrorWithData(
                RuntimeException("visitorRarities is empty, maybe visitor refusing was the cause?"),
                "Error rendering visitor drop statistics",
            )
        }
        addAsSingletonList(format(acceptedVisitors, "Accepted", "§2", ""))
        addAsSingletonList(format(deniedVisitors, "Denied", "§c", ""))
        addAsSingletonList("")
        addAsSingletonList(format(storage.copper, "Copper", "§c", ""))
        addAsSingletonList(format(storage.farmingExp, "Farming EXP", "§3", "§7"))
        addAsSingletonList(format(coinsSpent, "Coins Spent", "§6", ""))

        addAsSingletonList("")
        addAsSingletonList(format(storage.gardenExp, "Garden EXP", "§2", "§7"))
        addAsSingletonList(format(storage.bits, "Bits", "§b", "§b"))
        addAsSingletonList(format(storage.mithrilPowder, "Mithril Powder", "§2", "§2"))
        addAsSingletonList(format(storage.gemstonePowder, "Gemstone Powder", "§d", "§d"))

        for (reward in VisitorReward.entries) {
            val count = rewardsCount[reward] ?: 0
            if (config.displayIcons) { // Icons
                val stack = reward.itemStack
                if (config.displayNumbersFirst)
                    add(listOf("§b${count.addSeparators()} ", stack))
                else add(listOf(stack, " §b${count.addSeparators()}"))
            } else { // No Icons
                addAsSingletonList(format(count, reward.displayName, "§b"))
            }
        }
    }

    // Adding the mythic rarity between legendary and special, if missing
    private fun fixRaritiesSize(list: MutableList<Long>) {
        if (list.size == 4) {
            val special = list.last()
            list[3] = 0L
            list.add(special)
        }
    }

    fun format(amount: Number, name: String, color: String, amountColor: String = color) =
        if (config.displayNumbersFirst)
            "$color${format(amount)} $name"
        else
            "$color$name: $amountColor${format(amount)}"

    fun format(amount: Number): String {
        if (amount is Int) return amount.addSeparators()
        if (amount is Long) return amount.shortFormat()
        return "$amount"
    }

    // todo this should just save when changed not once a second
    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        saveAndUpdate()
    }

    fun saveAndUpdate() {
        if (!GardenAPI.inGarden()) return
        val storage = GardenAPI.storage?.visitorDrops ?: return
        storage.acceptedVisitors = acceptedVisitors
        storage.deniedVisitors = deniedVisitors
        totalVisitors = acceptedVisitors + deniedVisitors
        storage.coinsSpent = coinsSpent
        storage.rewardsCount = rewardsCount
        display = formatDisplay(drawDisplay(storage))
    }

    fun resetCommand() {
        val storage = GardenAPI.storage?.visitorDrops ?: return
        ChatUtils.clickableChat(
            "Click here to reset Visitor Drops Statistics.",
            onClick = {
                acceptedVisitors = 0
                deniedVisitors = 0
                totalVisitors = 0
                coinsSpent = 0
                storage.copper = 0
                storage.bits = 0
                storage.farmingExp = 0
                storage.gardenExp = 0
                storage.gemstonePowder = 0
                storage.mithrilPowder = 0
                storage.visitorRarities = arrayListOf(0, 0, 0, 0, 0)
                storage.rewardsCount = mapOf<VisitorReward, Int>()
                ChatUtils.chat("Visitor Drop Statistics reset!")
                saveAndUpdate()
            },
            "§eClick to reset!",
        )
    }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        val storage = GardenAPI.storage?.visitorDrops ?: return
        val visitorRarities = storage.visitorRarities
        if (visitorRarities.size == 0) {
            visitorRarities.add(0)
            visitorRarities.add(0)
            visitorRarities.add(0)
            visitorRarities.add(0)
            visitorRarities.add(0)
        }
        acceptedVisitors = storage.acceptedVisitors
        deniedVisitors = storage.deniedVisitors
        totalVisitors = acceptedVisitors + deniedVisitors
        coinsSpent = storage.coinsSpent
        rewardsCount = storage.rewardsCount
        saveAndUpdate()
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!config.enabled) return
        if (!GardenAPI.inGarden()) return
        if (GardenAPI.hideExtraGuis()) return
        if (config.onlyOnBarn && !GardenAPI.onBarnPlot) return
        config.pos.renderStringsAndItems(display, posLabel = "Visitor Stats")
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        val originalPrefix = "garden.visitorDropsStatistics."
        val newPrefix = "garden.visitors.dropsStatistics."
        event.move(3, "${originalPrefix}enabled", "${newPrefix}enabled")
        event.move(3, "${originalPrefix}textFormat", "${newPrefix}textFormat")
        event.move(3, "${originalPrefix}displayNumbersFirst", "${newPrefix}displayNumbersFirst")
        event.move(3, "${originalPrefix}displayIcons", "${newPrefix}displayIcons")
        event.move(3, "${originalPrefix}onlyOnBarn", "${newPrefix}onlyOnBarn")
        event.move(3, "${originalPrefix}visitorDropPos", "${newPrefix}pos")

        event.transform(11, "${newPrefix}textFormat") { element ->
            ConfigUtils.migrateIntArrayListToEnumArrayList(element, DropsStatisticsTextEntry::class.java)
        }
    }
}

enum class VisitorRarity {
    UNCOMMON,
    RARE,
    LEGENDARY,
    MYTHIC,
    SPECIAL,
}
