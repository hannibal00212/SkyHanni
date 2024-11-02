package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.CollectionUtils.addSearchString
import at.hannibal2.skyhanni.utils.LocationUtils.isPlayerInside
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.Searchable
import at.hannibal2.skyhanni.utils.renderables.toSearchable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import at.hannibal2.skyhanni.utils.tracker.ItemTrackerData
import at.hannibal2.skyhanni.utils.tracker.SkyHanniItemTracker
import com.google.gson.annotations.Expose
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object DraconicSacrificeTracker {

    private val config get() = SkyHanniMod.feature.combat.endIsland.draconicSacrificeTracker
    private val patternGroup = RepoPattern.group("misc.draconicsacrifice")

    /**
     * REGEX-TEST: §c§lSACRIFICE! §r§eYou turned §r§5Ender Boots §r§einto §r§d3 Dragon Essence§r§e!
     * REGEX-TEST: §c§lSACRIFICE! §r§eYou turned §r§5Ender Helmet §r§einto §r§d3 Dragon Essence§r§e!
     * REGEX-TEST: §c§lSACRIFICE! §r§eYou turned §r§6Old Dragon Helmet §r§einto §r§d25 Dragon Essence§r§e!
     * REGEX-TEST: §c§lSACRIFICE! §r§eYou turned §r§6Wise Dragon Helmet §r§einto §r§d25 Dragon Essence§r§e!
     */
    private val sacrificeLoot by patternGroup.pattern(
        "sacrifice",
        "§c§lSACRIFICE! §r§eYou turned §r(?<item>.*) §r§einto §r§d(?<amount>\\d+) Dragon Essence§r§e!",
    )

    /**
     * REGEX-TEST: §c§lBONUS LOOT! §r§eYou also received §r§817x §r§5Wise Dragon Fragment §r§efrom your sacrifice!
     */
    private val bonusLoot by patternGroup.pattern(
        "bonus",
        "§c§lBONUS LOOT! §r§eYou also received §r(?:§\\w(?<amount>\\d+)?x)?(?: §r)?(?<item>.*) §r§efrom your sacrifice!",
    )

    private val tracker =
        SkyHanniItemTracker("Draconic Sacrifice Profit Tracker", { Data() }, { it.draconicSacrificeTracker }) {
            drawDisplay(it)
        }

    private val altarArea = AxisAlignedBB(-601.0, 4.0, -282.0, -586.0, 15.0, -269.0)
    private val ESSENCE_DRAGON = "ESSENCE_DRAGON".asInternalName()

    class Data : ItemTrackerData() {
        override fun resetItems() {
            sacrificedItemsMap.clear()
            itemsSacrificed = 0
        }

        override fun getDescription(timesGained: Long): List<String> {
            val percentage = timesGained.toDouble() / itemsSacrificed
            val dropRate = LorenzUtils.formatPercentage(percentage.coerceAtMost(1.0))
            return listOf(
                "§7Dropped §e${timesGained.addSeparators()} §7times.",
                "§7Your drop rate: §c$dropRate.",
            )
        }

        override fun getCoinName(item: TrackedItem) = "§dDragon Essence"

        override fun getCoinDescription(item: TrackedItem): List<String> {
            val essences = item.totalAmount.addSeparators()
            return listOf(
                "§7Sacrificed items give you dragon essence.",
                "§7You got §6$essences essence §7that way.",
            )
        }

        @Expose
        var itemsSacrificed = 0L

        @Expose
        var sacrificedItemsMap: MutableMap<String, Long> = mutableMapOf()
    }

    private fun drawDisplay(data: Data): List<Searchable> = buildList {
        addSearchString("§5§lDraconic Sacrifice Profit Tracker")
        val profit = tracker.drawItems(data, { true }, this)

        add(
            Renderable.hoverTips(
                "§b${data.itemsSacrificed.addSeparators()} §6Items Sacrificed",
                data.sacrificedItemsMap.map { (item, amount) -> "$item: §b$amount" },
            ).toSearchable()
        )

        add(tracker.addTotalProfit(profit, data.itemsSacrificed, "sacrifice"))

        tracker.addPriceFromButton(this)
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        sacrificeLoot.matchMatcher(event.message) {
            val amount = group("amount").toInt()
            val item = group("item")
            tracker.addItem(ESSENCE_DRAGON, amount, command = false)
            tracker.modify {
                it.itemsSacrificed += 1
                it.sacrificedItemsMap.addOrPut(item, 1)
            }
        }

        bonusLoot.matchMatcher(event.message) {
            val item = group("item")
            val amount = groupOrNull("amount")?.toInt() ?: 1
            val internalName = NEUInternalName.fromItemNameOrNull(item) ?: return
            tracker.addItem(internalName, amount, command = false)
        }
        tracker.update()
    }

    @SubscribeEvent
    fun onRender(event: GuiRenderEvent) {
        if (!isEnabled()) return
        if (config.onlyInVoidSlate && !altarArea.isPlayerInside()) return

        tracker.renderDisplay(config.position)
    }

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("shresetdraconicsacrificetracker") {
            description = "Resets the Draconic Sacrifice Tracker."
            category = CommandCategory.USERS_RESET
            callback { tracker.resetCommand() }
        }
    }

    private fun isEnabled() = IslandType.THE_END.isInIsland() && config.enabled
}
