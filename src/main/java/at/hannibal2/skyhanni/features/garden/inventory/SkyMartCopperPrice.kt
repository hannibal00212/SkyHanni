package at.hannibal2.skyhanni.features.garden.inventory

import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.DisplayTableEntry
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.ItemUtils.loreCosts
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUItems.getPrice
import at.hannibal2.skyhanni.utils.NEUItems.getPriceOrNull
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import com.google.gson.JsonPrimitive
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object SkyMartCopperPrice {

    private val copperPattern by RepoPattern.pattern(
        "garden.inventory.skymart.copper",
        "§c(?<amount>.*) Copper"
    )

    private var display = emptyList<Renderable>()
    private val config get() = GardenAPI.config.skyMart

    var inInventory = false

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!isEnabled()) return
        if (!event.inventoryName.startsWith("SkyMart ")) return

        inInventory = true
        val table = mutableListOf<DisplayTableEntry>()
        for ((slot, item) in event.inventoryItems) {
            val lore = item.getLore()
            val otherItemsPrice = item.loreCosts().sumOf { it.getPrice() }.takeIf { it != -1.0 }

            for (line in lore) {
                val copper = copperPattern.matchMatcher(line) {
                    group("amount").formatInt()
                } ?: continue

                val internalName = item.getInternalName()
                val itemPrice = internalName.getPriceOrNull(config.priceSource) ?: continue
                val profit = itemPrice - (otherItemsPrice ?: 0.0)

                val factor = profit / copper
                val perFormat = factor.shortFormat()

                val itemName = item.itemName
                val hover = buildList {
                    add(itemName)
                    add("")
                    add("§7Item price: §6${itemPrice.shortFormat()} ")
                    otherItemsPrice?.let {
                        add("§7Additional cost: §6${it.shortFormat()} ")
                    }
                    add("§7Profit per purchase: §6${profit.shortFormat()} ")
                    add("")
                    add("§7Copper amount: §c${copper.addSeparators()} ")
                    add("§7Profit per copper: §6${perFormat} ")
                }
                table.add(
                    DisplayTableEntry(
                        "$itemName§f:",
                        "§6§l$perFormat",
                        factor,
                        internalName,
                        hover,
                        highlightsOnHoverSlots = listOf(slot)
                    )
                )
            }
        }

        val newList = mutableListOf<Renderable>()
        newList.add(Renderable.string("§eCoins per Copper§f:"))
        newList.add(LorenzUtils.fillTable(table, padding = 5, itemScale = config.itemScale))
        display = newList
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
    }

    @SubscribeEvent
    fun onBackgroundDraw(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (inInventory) {
            config.copperPricePos.renderRenderables(
                display,
                extraSpace = 5,
                posLabel = "SkyMart Copper Price"
            )
        }
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "garden.skyMartCopperPrice", "garden.skyMart.copperPrice")
        event.move(3, "garden.skyMartCopperPriceAdvancedStats", "garden.skyMart.copperPriceAdvancedStats")
        event.move(3, "garden.skyMartCopperPricePos", "garden.skyMart.copperPricePos")
        event.transform(32, "garden.skyMart.itemScale") {
            JsonPrimitive((it.asDouble / 1.851).roundTo(1))
        }
    }

    private fun isEnabled() = GardenAPI.inGarden() && config.copperPrice
}
