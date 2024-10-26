package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.makeShiftClick
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object ShiftClickBrewing {

    @SubscribeEvent
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!SkyHanniMod.feature.inventory.shiftClickBrewing) return

        if (event.gui !is GuiChest) return

        if (event.slot == null || event.slotId == 49) return

        val chestName = InventoryUtils.openInventoryName()
        if (!chestName.startsWith("Brewing Stand")) return

        event.makeShiftClick()
    }
}
