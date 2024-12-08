package at.hannibal2.skyhanni.features.garden.pests

import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.inventory.patternGroup
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.DisplayTableEntry
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPrice
import at.hannibal2.skyhanni.utils.ItemUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.formatDoubleOrNull
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object PesthunterProfit {

    data class PesthunterTrade(val internalName: NEUInternalName, val coinsPerPest: Double)
    var bestPesthunterTrade = mutableListOf<PesthunterTrade>()

    private val config get() = GardenAPI.config.pests.pesthunterShop
    private var display = emptyList<Renderable>()

    var inInventory = false

    /**
     * REGEX-TEST: §2100 Pests
     * REGEX-TEST: §21,500 Pests
     */
    private val pestCostPattern by patternGroup.pattern(
        "garden.pests.pesthunter.cost",
        "§2(?<pests>[\\d,]+) Pests"
    )

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!config.pesthunterProfitEnabled) return
        if (event.inventoryName != "Pesthunter's Wares") return

        inInventory = true

        val table = mutableListOf<DisplayTableEntry>()
        for ((slot, item) in event.inventoryItems) {
            try {
                readItem(slot, item, table)
            } catch (e: Throwable) {
                ErrorManager.logErrorWithData(
                    e, "Error in PesthunterProfit while reading item '${item.itemName}'",
                    "name" to item.itemName,
                    "inventory name" to InventoryUtils.openInventoryName(),
                )
            }
        }

        val newList = mutableListOf<Renderable>()
        newList.add(Renderable.string("§ePesthunter Shop Profit"))
        newList.add(LorenzUtils.fillTable(table, padding = 5, itemScale = 0.7))
        display = newList
    }

    private fun readItem(slot: Int, item: ItemStack, table: MutableList<DisplayTableEntry>) {
        val itemName = item.displayName
        if (itemName == " " || itemName == "§cClose") return
        if (itemName == "§aSell Item" || itemName == "§6Pesthunter's Wares") return

        val totalCost = getFullCost(getRequiredItems(item))
        if (totalCost < 0) return

        val (name, amount) = ItemUtils.readItemAmount(itemName) ?: return

        var internalName = NEUInternalName.fromItemNameOrNull(name.replace("[Lvl 100]", "[Lvl {LVL}]"))
        if (internalName == null) {
            internalName = item.getInternalName()
        }

        val itemPrice = internalName.getPrice() * amount
        if (itemPrice < 0) return

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

        table.add(
            DisplayTableEntry(
                itemName.replace("[Lvl 100]", "[Lvl 1]"), // show level 1 hedgehog instead of level 100
                "$color${profitPerPest.shortFormat()}",
                profitPerPest,
                internalName,
                hover,
                highlightsOnHoverSlots = listOf(slot),
            ),
        )

        if (bestPesthunterTrade.isEmpty() || profitPerPest > (
                bestPesthunterTrade.maxByOrNull {
                    it.coinsPerPest
                }?.coinsPerPest ?: 0.0
                )
        ) {
            bestPesthunterTrade.clear()
            bestPesthunterTrade.add(PesthunterTrade(internalName, profitPerPest))
        }
    }

    private fun getRequiredItems(item: ItemStack): MutableList<String> {
        val items = mutableListOf<String>()
        var next = false

        for (line in item.getLore()) {
            when {
                line == "§7Cost" -> {
                    next = true
                }
                next -> {
                    if (line.isBlank()) {
                        next = false
                    } else {
                        pestCostPattern.matchMatcher(line) { break }
                        items.add(line.replace("§8 ", " §8"))
                    }
                }
            }
        }

        return items
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

    private fun getPestsCost(item: ItemStack): Int {
        val lore = item.getLore()
        for (line in lore) {
            pestCostPattern.matchMatcher(line) {
                return group("pests")?.formatDoubleOrNull()?.toInt() ?: 0
            }
        }
        return 0
    }

    @SubscribeEvent
    fun onBackgroundDraw(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (inInventory) {
            config.pesthunterProfitPos.renderRenderables(
                display,
                extraSpace = 5,
                posLabel = "Pesthunter Profit",
            )
        }
    }
}
