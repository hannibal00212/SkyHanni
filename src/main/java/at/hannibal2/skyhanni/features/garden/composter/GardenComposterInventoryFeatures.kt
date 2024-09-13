package at.hannibal2.skyhanni.features.garden.composter

import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.InventoryUtils.getUpperItems
import at.hannibal2.skyhanni.utils.ItemUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUItems.getPrice
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object GardenComposterInventoryFeatures {

    private val config get() = GardenAPI.config.composters

    @SubscribeEvent
    fun onTooltip(event: LorenzToolTipEvent) {
        if (!GardenAPI.inGarden()) return
        if (!config.upgradePrice) return

        if (InventoryUtils.openInventoryName() != "Composter Upgrades") return

        var next = false
        val list = event.toolTip
        var i = -1
        var indexFullCost = 0
        var fullPrice = 0.0
        var amountItems = 0
        for (line in event.toolTipRemovedPrefix()) {
            i++
            if (line == "§7Upgrade Cost:") {
                next = true
                indexFullCost = i
                continue
            }

            if (next) {
                if (line.endsWith(" Copper")) continue
                if (line == "") break
                val (itemName, amount) = ItemUtils.readItemAmount(line) ?: run {
                    ErrorManager.logErrorStateWithData(
                        "Error reading item line",
                        "could not read item line",
                        "line" to line
                    )
                    continue
                }
                val internalName = NEUInternalName.fromItemName(itemName)
                val lowestBin = internalName.getPrice()
                val price = lowestBin * amount
                fullPrice += price
                val format = price.shortFormat()
                list[i] = list[i] + " §7(§6$format§7)"
                amountItems++
            }
        }

        if (amountItems > 1) {
            val format = fullPrice.shortFormat()
            list[indexFullCost] = list[indexFullCost] + " §7(§6$format§7)"
        }
    }

    @SubscribeEvent
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!config.highlightUpgrade) return

        if (InventoryUtils.openInventoryName() == "Composter Upgrades") {
            if (event.gui !is GuiChest) return
            val guiChest = event.gui
            val chest = guiChest.inventorySlots as ContainerChest

            for ((slot, stack) in chest.getUpperItems()) {
                if (stack.getLore().any { it == "§eClick to upgrade!" }) {
                    slot highlight LorenzColor.GOLD
                }
            }
        }
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "garden.composterUpgradePrice", "garden.composters.upgradePrice")
        event.move(3, "garden.composterHighLightUpgrade", "garden.composters.highlightUpgrade")
    }
}
