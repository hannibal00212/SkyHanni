package at.hannibal2.skyhanni.features.garden.visitor

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.garden.visitor.DropsStatisticsConfig.DropsStatisticsTextEntry
import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage.GardenStorage.VisitorDrops
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
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.CollectionUtils.editCopy
import at.hannibal2.skyhanni.utils.ConfigUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.renderables.Container
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object GardenVisitorDropStatistics {

    private val config get() = GardenAPI.config.visitors.dropsStatistics
    private var display: Renderable? = null

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
        "OFFER ACCEPTED with (?<visitor>.*) \\((?<rarity>.*)\\)",
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

    private val rewardsToEntries = mapOf(
        DropsStatisticsTextEntry.FLOWERING_BOUQUET to VisitorReward.FLOWERING_BOUQUET,
        DropsStatisticsTextEntry.OVERGROWN_GRASS to VisitorReward.OVERGROWN_GRASS,
        DropsStatisticsTextEntry.GREEN_BANDANA to VisitorReward.GREEN_BANDANA,
        DropsStatisticsTextEntry.DEDICATION_IV to VisitorReward.DEDICATION,
        DropsStatisticsTextEntry.MUSIC_RUNE_I to VisitorReward.MUSIC_RUNE,
        DropsStatisticsTextEntry.SPACE_HELMET to VisitorReward.SPACE_HELMET,
        DropsStatisticsTextEntry.CULTIVATING_I to VisitorReward.CULTIVATING,
        DropsStatisticsTextEntry.REPLENISH_I to VisitorReward.REPLENISH,
        DropsStatisticsTextEntry.DELICATE to VisitorReward.COPPER_DYE,
        DropsStatisticsTextEntry.COPPER_DYE to VisitorReward.SPACE_HELMET,
    )

    @HandleEvent
    fun onProfileJoin(event: ProfileJoinEvent) {
        display = null
    }

    @HandleEvent
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

    // Adding the mythic rarity between legendary and special, if missing
    private fun fixRaritiesSize(list: MutableList<Long>) {
        if (list.size == 4) {
            val special = list.last()
            list[3] = 0L
            list.add(special)
        }
    }

    fun format(amount: Number, name: String, color: String, amountColor: String = color) = Renderable.string(
        if (config.displayNumbersFirst)
            "$color${format(amount)} $name"
        else
            "$color$name: $amountColor${format(amount)}"
    )

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
        display = drawDisplay(storage)
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

    @HandleEvent
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
        config.pos.renderRenderable(display, posLabel = "Visitor Stats")
    }

    @HandleEvent
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

    private fun drawDisplay(storage: VisitorDrops) = Container.vertical {
        for (entry in config.textFormat) {
            renderable(formatEntry(storage, entry))
        }
    }

    private fun formatVisitorRewardEntry(reward: VisitorReward): Renderable {
        val count = rewardsCount[reward] ?: 0
        return when {
            !config.displayIcons -> format(count, reward.displayName, "§b")
            config.displayNumbersFirst -> Container.horizontal {
                string("§b${count.addSeparators()} ")
                item(reward.itemStack)
            }
            else -> Container.horizontal {
                item(reward.itemStack)
                string(" §b${count.addSeparators()}")
            }
        }
    }

    private fun formatEntry(storage: VisitorDrops, entry: DropsStatisticsTextEntry): Renderable {
        return when (entry) {
            DropsStatisticsTextEntry.TITLE -> Renderable.string("§e§lVisitor Statistics")
            DropsStatisticsTextEntry.TOTAL_VISITORS -> format(totalVisitors, "Total", "§e", "")
            DropsStatisticsTextEntry.ACCEPTED -> format(acceptedVisitors, "Accepted", "§2", "")
            DropsStatisticsTextEntry.DENIED -> format(deniedVisitors, "Denied", "§c", "")
            DropsStatisticsTextEntry.SPACER_1 -> Renderable.placeholder(0, 10)
            DropsStatisticsTextEntry.SPACER_2 -> Renderable.placeholder(0, 10)
            DropsStatisticsTextEntry.COPPER -> format(storage.copper, "Copper", "§c", "")
            DropsStatisticsTextEntry.FARMING_EXP -> format(storage.farmingExp, "Farming EXP", "§3", "§7")
            DropsStatisticsTextEntry.COINS_SPENT -> format(coinsSpent, "Coins Spent", "§6", "")
            DropsStatisticsTextEntry.GARDEN_EXP -> format(storage.gardenExp, "Garden EXP", "§2", "§7")
            DropsStatisticsTextEntry.BITS -> format(storage.bits, "Bits", "§b", "§b")
            DropsStatisticsTextEntry.MITHRIL_POWDER -> format(storage.mithrilPowder, "Mithril Powder", "§2", "§2")
            DropsStatisticsTextEntry.GEMSTONE_POWDER -> format(storage.gemstonePowder, "Gemstone Powder", "§d", "§d")
            DropsStatisticsTextEntry.VISITORS_BY_RARITY -> {
                val visitorRarities = storage.visitorRarities
                fixRaritiesSize(visitorRarities)
                if (visitorRarities.isNotEmpty()) {
                    Renderable.string(
                        "§a${visitorRarities[0].addSeparators()}§f-" +
                            "§9${visitorRarities[1].addSeparators()}§f-" +
                            "§6${visitorRarities[2].addSeparators()}§f-" +
                            "§d${visitorRarities[3].addSeparators()}§f-" +
                            "§c${visitorRarities[4].addSeparators()}",
                    )
                } else {
                    ErrorManager.logErrorWithData(
                        RuntimeException("visitorRarities is empty, maybe visitor refusing was the cause?"),
                        "Error rendering visitor drop statistics",
                    )
                    Renderable.string("§c?")
                }
            }
            else -> {
                val reward = rewardsToEntries[entry] ?: return Renderable.string("§c?")
                formatVisitorRewardEntry(reward)
            }
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
