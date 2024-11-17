package at.hannibal2.skyhanni.features.garden.inventory.plots

import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPrice
import at.hannibal2.skyhanni.utils.ItemUtils
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object GardenNextPlotPrice {

    @SubscribeEvent
    fun onTooltip(event: LorenzToolTipEvent) {
        if (!GardenAPI.inGarden()) return
        if (!GardenAPI.config.plotPrice) return

        if (InventoryUtils.openInventoryName() != "Configure Plots") return

        if (!event.itemStack.name.startsWith("§ePlot")) return

        var next = false
        val list = event.toolTip
        var i = -1
        for (line in event.toolTipRemovedPrefix()) {
            i++
            if (line.contains("Cost")) {
                next = true
                continue
            }

            if (next) {
                val readItemAmount = ItemUtils.readItemAmount(line)
                readItemAmount?.let {
                    val (itemName, amount) = it
                    val lowestBin = NEUInternalName.fromItemName(itemName).getPrice()
                    val price = lowestBin * amount
                    val format = price.shortFormat()
                    list[i] = list[i] + " §7(§6$format§7)"
                } ?: run {
                    ErrorManager.logErrorStateWithData(
                        "Garden Next Plot Price error",
                        "Could not read item amount from line",
                        "line" to line,
                        "event.toolTip" to event.toolTip,
                    )
                }
                break
            }
        }
    }
}
