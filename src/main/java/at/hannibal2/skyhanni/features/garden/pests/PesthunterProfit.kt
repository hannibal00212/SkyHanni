package at.hannibal2.skyhanni.features.garden.pests

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.indexOfFirstOrNull
import at.hannibal2.skyhanni.utils.DisplayTableEntry
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPrice
import at.hannibal2.skyhanni.utils.ItemUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.formatDoubleOrNull
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.item.ItemStack

@SkyHanniModule
object PesthunterProfit {

    private val config get() = GardenAPI.config.pests.pesthunterShop
    private val patternGroup = RepoPattern.group("garden.pests.pesthunter")
    private val DENY_LIST_ITEMS = listOf(
        "§cClose",
        "§6Pesthunter's Wares",
        " ",
    )
    private var display = emptyList<Renderable>()
    private val tradeProfits = mutableListOf<Double>()
    private var inInventory = false

    /**
     * REGEX-TEST: §2100 Pests
     * REGEX-TEST: §21,500 Pests
     */
    private val pestCostPattern by patternGroup.pattern(
        "garden.pests.pesthunter.cost",
        "§2(?<pests>[\\d,]+) Pests"
    )

    fun isInInventory() = inInventory

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
        tradeProfits.clear()
    }

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!config.pesthunterProfitEnabled) return
        if (event.inventoryName != "Pesthunter's Wares") return

        inInventory = true
        display = buildRenderables(event.inventoryItems)
    }

    private fun buildRenderables(items: Map<Int, ItemStack>) = buildList {
        val table = items.mapNotNull { (slot, stack) -> readItem(slot, stack) }
        add(Renderable.string("§ePesthunter Shop Profit"))
        add(LorenzUtils.fillTable(table, padding = 5, itemScale = 0.7))
    }

    private fun readItem(slot: Int, item: ItemStack): DisplayTableEntry? {
        val itemName = item.displayName.takeIf {
            it !in DENY_LIST_ITEMS && it.trim().isNotEmpty()
        } ?: return null
        if (slot == 49) return null

        val totalCost = getFullCost(getRequiredItems(item)).takeIf { it >= 0 } ?: return null
        val (name, amount) = ItemUtils.readItemAmount(itemName) ?: return null
        val fixedDisplayName = name.replace("[Lvl 100]", "[Lvl {LVL}]")
        val internalName = NEUInternalName.fromItemNameOrNull(fixedDisplayName)
            ?: item.getInternalName()

        val itemPrice = (internalName.getPrice() * amount).takeIf { it >= 0 } ?: return null

        val profit = itemPrice - totalCost
        val pestsCost = getPestsCost(item)
        val profitPerPest = if (pestsCost > 0) profit / pestsCost else 0.0
        val color = if (profitPerPest > 0) "§6" else "§c"

        val hover = listOf(
            itemName.replace("[Lvl 100]", "[Lvl 1]"),
            "",
            "§7Item price: §6${itemPrice.shortFormat()} ",
            "§7Material cost: §6${totalCost.shortFormat()} ",
            "§7Final profit: §6${profit.shortFormat()} ",
            "§7Profit per pest: §6${profitPerPest.shortFormat()} ",
        )

        return DisplayTableEntry(
            itemName.replace("[Lvl 100]", "[Lvl 1]"), // show level 1 hedgehog instead of level 100
            "$color${profitPerPest.shortFormat()}",
            profitPerPest,
            internalName,
            hover,
            highlightsOnHoverSlots = listOf(slot),
        )
    }

    private fun getRequiredItems(item: ItemStack): List<String> {
        val lore = item.getLore().filter { !pestCostPattern.matches(it) }

        val startIndex = lore.indexOf("§7Cost") + 1
        val endIndex = lore.indexOfFirstOrNull { it.isBlank() && lore.indexOf(it) > startIndex } ?: lore.size

        return lore.subList(startIndex, endIndex).map { it.replace("§8 ", " §8") }
    }

    private fun getFullCost(requiredItems: List<String>): Double  = requiredItems.mapNotNull {
        ItemUtils.readItemAmount(it)
    }.sumOf { (name, amount) ->
        val internalName = NEUInternalName.fromItemNameOrNull(name) ?: return@sumOf 0.0
        internalName.getPrice() * amount
    }

    private fun getPestsCost(item: ItemStack): Int = pestCostPattern.firstMatcher(item.getLore()) {
        group("pests")?.formatDoubleOrNull()?.toInt() ?: 0
    } ?: 0

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onBackgroundDraw(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (!inInventory) return
        config.pesthunterProfitPos.renderRenderables(
            display,
            extraSpace = 5,
            posLabel = "Pesthunter Profit",
        )
    }
}
