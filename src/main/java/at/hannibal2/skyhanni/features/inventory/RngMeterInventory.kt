package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.RenderItemTipEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.between
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object RngMeterInventory {

    private val config get() = SkyHanniMod.feature.inventory.rngMeter

    @HandleEvent
    fun onRenderItemTip(event: RenderItemTipEvent) {
        val chestName = InventoryUtils.openInventoryName()

        val stack = event.stack
        if (config.floorName && chestName == "Catacombs RNG Meter") {
            if (stack.name.removeColor() == "RNG Meter") {
                event.stackTip = stack.getLore()[0].between("(", ")")
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!LorenzUtils.inSkyBlock) return

        val chestName = InventoryUtils.openInventoryName()
        if (config.noDrop && chestName == "Catacombs RNG Meter") {
            for (slot in InventoryUtils.getItemsInOpenChest()) {
                val stack = slot.stack
                if (stack.getLore().any { it.contains("You don't have an RNG drop") }) {
                    slot highlight LorenzColor.RED
                }
            }
        }

        if (config.selectedDrop && chestName.endsWith(" RNG Meter")) {
            for (slot in InventoryUtils.getItemsInOpenChest()) {
                val stack = slot.stack
                if (stack.getLore().any { it.contains("§a§lSELECTED") }) {
                    slot highlight LorenzColor.YELLOW
                }
            }
        }
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "inventory.rngMeterFloorName", "inventory.rngMeter.floorName")
        event.move(3, "inventory.rngMeterNoDrop", "inventory.rngMeter.noDrop")
        event.move(3, "inventory.rngMeterSelectedDrop", "inventory.rngMeter.selectedDrop")
    }
}
