package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.RenderInventoryItemTipEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.name

@SkyHanniModule
object BrewingStandOverlay {

    @HandleEvent(onlyOnSkyblock = true)
    fun onRenderItemTip(event: RenderInventoryItemTipEvent) {
        if (!SkyHanniMod.feature.misc.brewingStandOverlay) return

        if (event.inventoryName != "Brewing Stand") return

        val stack = event.stack
        val name = stack.name

        val slotNumber = event.slot.slotNumber
        when (slotNumber) {
            13, // Ingredient input
            21, // Progress
            42, // Output right side
            -> Unit

            else -> return
        }

        if (slotNumber == 21) {
            event.offsetX = 55
        }

        // Hide the progress slot when not active
        if (name.contains(" or ")) return

        event.stackTip = name
        event.offsetX += 3
        event.offsetY = -5
        event.alignLeft = false
    }
}
