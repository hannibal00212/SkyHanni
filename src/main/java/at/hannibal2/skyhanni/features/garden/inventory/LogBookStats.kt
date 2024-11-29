package at.hannibal2.skyhanni.features.garden.inventory

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.garden.visitor.VisitorAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.init.Items
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object LogBookStats {

    private val groupPattern = RepoPattern.group("garden.inventory.logbook")

    /**
     * REGEX-TEST: §7Times Visited: §a22
     */
    private val visitedPattern by groupPattern.pattern(
        "visited",
        "§7Times Visited: §a(?<timesVisited>[0-9,.]+)",
    )

    /**
     * REGEX-TEST: §7Offers Accepted: §a21
     */
    private val acceptedPattern by groupPattern.pattern(
        "accepted",
        "§7Offers Accepted: §a(?<timesAccepted>[0-9,.]+)",
    )

    /**
     * REGEX-TEST: §ePage 3
     */
    private val pagePattern by groupPattern.pattern(
        "page.current",
        "§ePage (?<page>\\d)",
    )

    private val config get() = GardenAPI.config
    private var display = emptyList<Renderable>()
    private val loggedVisitors = mutableMapOf<Int, List<VisitorInfo>>()
    private var inInventory = false
    private var currentPage = 0

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (IslandType.GARDEN_GUEST.isInIsland()) return
        val inventoryName = event.inventoryName
        if (inventoryName != "Visitor's Logbook") return

        inInventory = true
        checkPages(event)
        val list = mutableListOf<VisitorInfo>()

        for ((index, item) in event.inventoryItems) {
            val visitorName = item.displayName ?: continue
            var timesVisited = 0L
            var timesAccepted = 0L
            val lore = item.getLore()
            visitedPattern.firstMatcher(lore) {
                timesVisited += group("timesVisited").formatLong()
            }
            acceptedPattern.firstMatcher(lore) {
                timesAccepted += group("timesAccepted").formatLong()
            }

            val visitor = VisitorInfo(index, visitorName, timesVisited, timesAccepted)
            list.add(visitor)
        }
        loggedVisitors[currentPage] = list
        display = buildList {
            val visited = loggedVisitors.values.sumOf { it.sumOf { visitor -> visitor.timesVisited } }
            val accepted = loggedVisitors.values.sumOf { it.sumOf { visitor -> visitor.timesAccepted } }
            val visitingNow = VisitorAPI.getVisitors().size
            val denied = visited - accepted - visitingNow
            add(Renderable.string("§6Times Visited: §b${visited.addSeparators()}"))
            add(Renderable.string("§6Times Accepted: §a${accepted.addSeparators()}"))
            add(Renderable.string("§6Times Denied: §c${denied.addSeparators()}"))
        }
    }

    @SubscribeEvent
    fun onBackgroundDraw(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (IslandType.GARDEN_GUEST.isInIsland()) return
        if (inInventory && config.showLogBookStats) {
            config.logBookStatsPos.renderRenderables(
                display,
                extraSpace = 5,
                posLabel = "Visitor's LogBook Stats",
            )
        }
    }

    @SubscribeEvent
    fun onProfileChange(event: ProfileJoinEvent) {
        display = emptyList()
        loggedVisitors.clear()
        currentPage = 0
        inInventory = false
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
    }

    private fun checkPages(event: InventoryFullyOpenedEvent) {
        val next = event.inventoryItems[53]
        if (next?.item != Items.arrow) {
            currentPage++
            return
        }
        for (item in event.inventoryItems.values) {
            if (item.displayName != "§aNext Page") continue
            pagePattern.firstMatcher(item.getLore()) {
                currentPage = group("page").toInt() - 1
            }
        }
    }

    data class VisitorInfo(
        var index: Int = -1,
        var displayName: String = "",
        var timesVisited: Long = 0,
        var timesAccepted: Long = 0,
    )
}
