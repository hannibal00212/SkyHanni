package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityAPI
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEventSummary
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils.removeResets
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.Searchable
import at.hannibal2.skyhanni.utils.renderables.toSearchable
import at.hannibal2.skyhanni.utils.tracker.SkyHanniTracker
import at.hannibal2.skyhanni.utils.tracker.TrackerData
import com.google.gson.annotations.Expose
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object ChocolateFactoryStrayTracker {

    private val config get() = ChocolateFactoryAPI.config
    private var claimedStraysSlots = mutableListOf<Int>()

    /**
     * REGEX-TEST: §9Zero §d§lCAUGHT!
     * REGEX-TEST: §6§lGolden Rabbit §d§lCAUGHT!
     * REGEX-TEST: §fAudi §d§lCAUGHT!
     */
    private val strayCaughtPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "stray.caught",
        "^§[a-f0-9].* §d§lCAUGHT!",
    )

    /**
     * REGEX-TEST: §7You caught a stray §fMandy §7and §7gained §6+283,574 Chocolate§7!
     * REGEX-TEST: §7You caught a stray §aSven §7and gained §7§6+397,004 Chocolate§7!'
     */
    private val strayLorePattern by ChocolateFactoryAPI.patternGroup.pattern(
        "stray.loreinfo",
        "§7You caught a stray (?<rabbit>(?<name>§[^§]*)) .* (?:§7)?§6\\+(?<amount>[\\d,]*) Chocolate§7!",
    )

    /**
     * REGEX-TEST: §7You caught a stray §6§lGolden Rabbit§7! §7You gained §6+13,566,571 Chocolate§7!
     */
    private val goldenStrayJackpotMountainPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "stray.goldenrawchoc",
        "§7You caught a stray §6§lGolden Rabbit§7! §7You gained §6\\+(?<amount>[\\d,]*) Chocolate§7!",
    )

    /**
     * REGEX-TEST: §7You caught a stray §6§lGolden Rabbit§7! §7You caught a glimpse of §6El Dorado§7, §7but he escaped and left behind §7§6313,780 Chocolate§7!
     * REGEX-TEST: §7You caught a stray §6§lGolden Rabbit§7! §7You caught §6El Dorado §7- quite the elusive rabbit!
     * REGEX-TEST: §7You caught a stray §6§lGolden Rabbit§7! §7You caught §6El Dorado§7! Since you §7already have captured him before, §7you gained §6+324,364,585 Chocolate§7.
     */
    private val strayDoradoPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "stray.dorado",
        ".*§6El Dorado(?:.*?§6\\+?(?<amount>[\\d,]+) Chocolate)?.*",
    )

    /**
     * REGEX-TEST: §7A hoard of §aStray Rabbits §7has appeared!
     * REGEX-TEST: §r§7A hoard of §r§aStray Rabbits §r§7has appeared!
     */
    private val strayHoardPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "stray.hoard",
        ".*(?:§r)?§7A hoard of (?:§r)?§aStray Rabbits (?:§r)?§7has.*",
    )

    /**
     * REGEX-TEST: §7You caught a stray §6§lGolden Rabbit§7! §7You gained §6+5 Chocolate §7until the §7end of the SkyBlock year!
     *
     */
    private val goldenStrayClick by ChocolateFactoryAPI.patternGroup.pattern(
        "stray.goldenclick",
        "§7You caught a stray §6§lGolden Rabbit§7! §7You gained §6\\+5 Chocolate §7until the §7end of the SkyBlock year!",
    )

    /**
     * REGEX-TEST: §7You caught a stray §9Fish the Rabbit§7! §7You have already found §9Fish the §9Rabbit§7, so you received §655,935,257 §6Chocolate§7!
     */
    private val fishTheRabbitPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "stray.fish",
        "§7You caught a stray (?<color>§.)Fish the Rabbit§7! §7You have already found (?:§.)?Fish the (?:§.)?Rabbit§7, so you received §6(?<amount>[\\d,]*) (?:§6)?Chocolate§7!",
    )

    private val rarityFormatMap = mapOf(
        "common" to "§f",
        "uncommon" to "§a",
        "rare" to "§9",
        "epic" to "§5",
        "legendary" to "§6",
    )

    private val tracker = SkyHanniTracker("Stray Tracker", { Data() }, { it.chocolateFactory.strayTracker })
    { drawDisplay(it) }

    class Data : TrackerData() {
        override fun reset() {
            straysCaught.clear()
            straysExtraChocMs.clear()
            goldenTypesCaught.clear()
        }

        @Expose
        var straysCaught: MutableMap<String, Int> = mutableMapOf()

        @Expose
        var straysExtraChocMs: MutableMap<String, Long> = mutableMapOf()

        @Expose
        var goldenTypesCaught: MutableMap<String, Int> = mutableMapOf()
    }

    private fun formLoreToSingleLine(lore: List<String>): String {
        val notEmptyLines = lore.filter { it.isNotEmpty() }
        return notEmptyLines.joinToString(" ")
    }

    private fun incrementRarity(rarity: String, chocAmount: Long = 0) {
        tracker.modify { it.straysCaught.addOrPut(rarity, 1) }
        val extraTime = ChocolateFactoryAPI.timeUntilNeed(chocAmount + 1)
        tracker.modify { it.straysExtraChocMs.addOrPut(rarity, extraTime.inWholeMilliseconds) }
        if (HoppityAPI.isHoppityEvent()) {
            LorenzRarity.getByName(rarity)?.let {
                HoppityEventSummary.addStrayCaught(it, chocAmount)
            }
        }
    }

    private fun incrementGoldenType(typeCaught: String, amount: Int = 1) {
        tracker.modify { it.goldenTypesCaught.addOrPut(typeCaught, amount) }
    }

    private fun drawDisplay(data: Data): List<Searchable> = buildList {
        val extraChocMs = data.straysExtraChocMs.values.sum().milliseconds
        val formattedExtraTime = extraChocMs.let { if (it == 0.milliseconds) "0s" else it.format() }

        add(
            Renderable.hoverTips(
                "§6§lStray Tracker",
                tips = listOf("§a+§b${formattedExtraTime} §afrom strays§7"),
            ).toSearchable(),
        )
        rarityFormatMap.keys.forEach { rarity ->
            extractHoverableOfRarity(rarity, data)?.let { add(it) }
        }
    }

    private fun extractHoverableOfRarity(rarity: String, data: Data): Searchable? {
        val caughtOfRarity = data.straysCaught[rarity]
        val caughtString = caughtOfRarity?.toString() ?: return null

        val rarityExtraChocMs = data.straysExtraChocMs[rarity]?.milliseconds
        val extraChocFormat = rarityExtraChocMs?.format() ?: ""

        val colorCode = rarityFormatMap[rarity] ?: ""
        val lineHeader = "$colorCode${rarity.substring(0, 1).uppercase()}${rarity.substring(1)}§7: §r$colorCode"
        val lineFormat = "${lineHeader}${caughtString}"

        val renderable = rarityExtraChocMs?.let {
            val tip =
                "§a+§b$extraChocFormat §afrom $colorCode$rarity strays§7${if (rarity == "legendary") extractGoldenTypesCaught(data) else ""}"
            Renderable.hoverTips(Renderable.string(lineFormat), tips = tip.split("\n"))
        } ?: Renderable.string(lineFormat)
        return renderable.toSearchable(rarity)
    }

    private fun extractGoldenTypesCaught(data: Data): String {
        val goldenList = mutableListOf<String>()
        data.goldenTypesCaught["sidedish"]?.let {
            goldenList.add("§b$it §6Side Dish" + if (it > 1) "es" else "")
        }
        data.goldenTypesCaught["jackpot"]?.let {
            goldenList.add("§b$it §6Chocolate Jackpot" + if (it > 1) "s" else "")
        }
        data.goldenTypesCaught["mountain"]?.let {
            goldenList.add("§b$it §6Chocolate Mountain" + if (it > 1) "s" else "")
        }
        data.goldenTypesCaught["dorado"]?.let {
            goldenList.add((if (it >= 3) "§a" else "§b") + "$it§7/§a3 §6El Dorado §7Sighting" + if (it > 1) "s" else "")
        }
        data.goldenTypesCaught["stampede"]?.let {
            goldenList.add("§b$it §6Stampede" + if (it > 1) "s" else "")
        }
        data.goldenTypesCaught["goldenclick"]?.let {
            goldenList.add("§b$it §6Golden Click" + if (it > 1) "s" else "")
        }
        return if (goldenList.size == 0) "" else ("\n" + goldenList.joinToString("\n"))
    }

    @SubscribeEvent
    fun onTick(event: SecondPassedEvent) {
        if (!isEnabled()) return
        InventoryUtils.getItemsInOpenChest().filter {
            !claimedStraysSlots.contains(it.slotIndex)
        }.forEach {
            strayCaughtPattern.matchMatcher(it.stack.name) {
                if (it.stack.getLore().isEmpty()) return
                claimedStraysSlots.add(it.slotIndex)
                val loreLine = formLoreToSingleLine(it.stack.getLore())

                // "Base" strays - Common -> Epic, raw choc only reward.
                strayLorePattern.matchMatcher(loreLine) {
                    //Pretty sure base strays max at Epic, but...
                    val rarity = rarityFormatMap.entries.find { e -> e.value == group("rabbit").substring(0, 2) }?.key ?: "common"
                    incrementRarity(rarity, group("amount").formatLong())
                }

                // Fish the Rabbit
                fishTheRabbitPattern.matchMatcher(loreLine) {
                    //Also fairly sure that Fish maxes out at Rare, but...
                    val rarity = rarityFormatMap.entries.find { e -> e.value == group("color").substring(0, 2) }?.key ?: "common"
                    incrementRarity(rarity, group("amount").formatLong())
                }

                // Golden Strays, Jackpot and Mountain, raw choc only reward.
                goldenStrayJackpotMountainPattern.matchMatcher(loreLine) {
                    val amount = group("amount").formatLong().also { am -> incrementRarity("legendary", am) }
                    val multiplier = amount / ChocolateFactoryAPI.chocolatePerSecond
                    when (multiplier) {
                        in 479.0..481.0 -> incrementGoldenType("jackpot")
                        in 1499.0..1501.0 -> incrementGoldenType("mountain")
                    }
                }

                // Golden Strays, "Golden Click"
                goldenStrayClick.matchMatcher(loreLine) {
                    incrementGoldenType("goldenclick")
                }

                // Golden Strays, hoard/stampede
                strayHoardPattern.matchMatcher(loreLine.removeResets()) {
                    incrementGoldenType("stampede")
                }

                // El Dorado - all catches
                strayDoradoPattern.matchMatcher(loreLine) {
                    groupOrNull("amount")?.let { amount ->
                        incrementRarity("legendary", amount.formatLong())
                    }
                    incrementGoldenType("dorado")
                }
            }
        }
        InventoryUtils.getItemsInOpenChest().filter {
            claimedStraysSlots.contains(it.slotIndex)
        }.forEach {
            if (!strayCaughtPattern.matches(it.stack.name)) {
                claimedStraysSlots.removeAt(claimedStraysSlots.indexOf(it.slotIndex))
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        val index = event.slot?.slotIndex ?: return
        if (index == -999) return
        if (claimedStraysSlots.contains(index)) return

        val clickedStack = InventoryUtils.getItemsInOpenChest()
            .find { it.slotNumber == event.slot.slotNumber && it.hasStack }
            ?.stack ?: return
        val nameText = (if (clickedStack.hasDisplayName()) clickedStack.displayName else clickedStack.itemName)
        if (!nameText.equals("§6§lGolden Rabbit §8- §aSide Dish")) return

        HoppityAPI.fireSideDishMessage()
        if (!isEnabled()) return

        claimedStraysSlots.add(index)
        incrementGoldenType("sidedish")
        incrementRarity("legendary", 0)
        DelayedRun.runDelayed(1.seconds) {
            claimedStraysSlots.remove(claimedStraysSlots.indexOf(index))
        }
    }

    @SubscribeEvent
    fun onBackgroundDraw(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (!isEnabled()) return
        tracker.renderDisplay(config.strayRabbitTrackerPosition)
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!isEnabled()) return
        tracker.firstUpdate()
    }

    fun resetCommand() {
        tracker.resetCommand()
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.strayRabbitTracker && ChocolateFactoryAPI.inChocolateFactory
}
