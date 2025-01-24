package at.hannibal2.skyhanni.features.garden.pests

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
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
    private var bestPesthunterTrade = mutableListOf<PesthunterTrade>()
    private var inInventory = false

    data class PesthunterTrade(val internalName: NEUInternalName, val coinsPerPest: Double)

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
        val itemName = item.displayName.takeIf { it !in DENY_LIST_ITEMS } ?: return null
        if (slot == 49) return null

        val totalCost = getFullCost(getRequiredItems(item)).takeIf { it > 0 } ?: return null
        val (name, amount) = ItemUtils.readItemAmount(itemName) ?: return null
        val fixedDisplayName = name.replace("[Lvl 100]", "[Lvl {LVL}]")
        val internalName = NEUInternalName.fromItemNameOrNull(fixedDisplayName)
            ?: item.getInternalName()

        val itemPrice = (internalName.getPrice() * amount).takeIf { it > 0 } ?: return null

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

        val bestTrade = bestPesthunterTrade.maxOfOrNull { it.coinsPerPest } ?: 0.0
        if (bestPesthunterTrade.isEmpty() || profitPerPest > bestTrade) {
            bestPesthunterTrade.clear()
            bestPesthunterTrade.add(PesthunterTrade(internalName, profitPerPest))
        }

        return DisplayTableEntry(
            itemName.replace("[Lvl 100]", "[Lvl 1]"), // show level 1 hedgehog instead of level 100
            "$color${profitPerPest.shortFormat()}",
            profitPerPest,
            internalName,
            hover,
            highlightsOnHoverSlots = listOf(slot),
        )
    }

    private fun getRequiredItems(item: ItemStack): MutableList<String> {
        val lore = item.getLore()
        // Find the subsection of lore between "§7Cost" and the next empty line
        val startIndex = lore.indexOf("§7Cost") + 1
        val endIndex = lore.indexOfFirst { it.isBlank() && lore.indexOf(it) > startIndex }

        val costLore =
            if (endIndex != -1)  lore.subList(startIndex, endIndex)
            else lore.subList(startIndex, lore.size)

        return costLore.filter {
            !pestCostPattern.matches(it)
        }.map {
            it.replace("§8 ", " §8")
        }.toMutableList()
    }

    private fun getFullCost(requiredItems: List<String>): Double {
        var otherItemsPrice = 0.0
        for (itemName in requiredItems) {
            val pair = ItemUtils.readItemAmount(itemName)
            if (pair == null) {
                ErrorManager.logErrorStateWithData(
                    "Error in Pesthunter Profit", "Could not read item amount",
                    "itemName" to itemName,
                )
                continue
            }

            val (name, amount) = pair
            otherItemsPrice += NEUInternalName.fromItemName(name).getPrice() * amount
        }
        return otherItemsPrice
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
