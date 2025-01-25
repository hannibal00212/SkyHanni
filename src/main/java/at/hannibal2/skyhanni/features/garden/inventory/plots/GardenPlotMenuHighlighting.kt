package at.hannibal2.skyhanni.features.garden.inventory.plots

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.features.garden.PlotMenuHighlightingConfig.PlotStatusType
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryUpdatedEvent
import at.hannibal2.skyhanni.features.garden.GardenApi
import at.hannibal2.skyhanni.features.garden.GardenPlotApi
import at.hannibal2.skyhanni.features.garden.GardenPlotApi.currentSpray
import at.hannibal2.skyhanni.features.garden.GardenPlotApi.isBeingPasted
import at.hannibal2.skyhanni.features.garden.GardenPlotApi.locked
import at.hannibal2.skyhanni.features.garden.GardenPlotApi.pests
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.RenderUtils.highlight

@SkyHanniModule
object GardenPlotMenuHighlighting {

    private val config get() = GardenApi.config.plotMenuHighlighting

    private val highlightedPlots = mutableMapOf<GardenPlotApi.Plot, PlotStatusType>()

    @HandleEvent
    fun onInventoryUpdated(event: InventoryUpdatedEvent) {
        if (!isEnabled()) return

        for (slot in InventoryUtils.getItemsInOpenChest()) {
            val list = mutableListOf<PlotStatusType>()
            val plot = GardenPlotApi.plots.find { it.inventorySlot == slot.slotIndex } ?: continue

            val (pestsEnabled, spraysEnabled, locksEnabled, currentEnabled, pastesEnabled) =
                PlotStatusType.entries.map { it in config.deskPlotStatusTypes }

            if (plot.pests >= 1 && pestsEnabled) list.add(PlotStatusType.PESTS)
            if (plot.currentSpray != null && spraysEnabled) list.add(PlotStatusType.SPRAYS)
            if (plot.locked && locksEnabled) list.add(PlotStatusType.LOCKED)
            if (plot == GardenPlotApi.getCurrentPlot() && currentEnabled) list.add(PlotStatusType.CURRENT)
            if (plot.isBeingPasted && pastesEnabled) list.add(PlotStatusType.PASTING)

            getLowestIndexItem(list)?.let { index ->
                val status = config.deskPlotStatusTypes[index]
                handleCurrent(plot, status)
            } ?: highlightedPlots.remove(plot)
        }
    }

    @HandleEvent
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!isEnabled() || highlightedPlots.isEmpty()) return

        for (plot in highlightedPlots) {
            val items = InventoryUtils.getItemsInOpenChest()
            if (plot.key.inventorySlot in items.indices) {
                val slot = items[plot.key.inventorySlot]
                slot.stack.stackSize = handleStackSize(plot.key, plot.value)
                slot highlight plot.value.highlightColor
            }
        }
    }

    private fun handleStackSize(plot: GardenPlotApi.Plot, status: PlotStatusType): Int {
        return when (status.name) {
            "§cPests" -> return plot.pests
            "§eSprays" -> return plot.currentSpray?.expiry?.timeUntil()?.inWholeMinutes?.toInt() ?: 1
            else -> 1
        }
    }

    private fun handleCurrent(plot: GardenPlotApi.Plot, status: PlotStatusType) {
        val isHighlighted = highlightedPlots.containsKey(plot)
        val isCurrent = highlightedPlots[plot] == status
        if (!isHighlighted || isCurrent) {
            if (!isHighlighted) highlightedPlots[plot] = status
        } else {
            highlightedPlots[plot] = status
        }
    }

    private fun getLowestIndexItem(array: MutableList<PlotStatusType>): Int? {
        return array.mapNotNull { status -> config.deskPlotStatusTypes.find { it == status } }
            .minOfOrNull { config.deskPlotStatusTypes.indexOf(it) }
    }

    private fun isEnabled() =
        GardenApi.inGarden() && InventoryUtils.openInventoryName() == "Configure Plots" && config.enabled
}
