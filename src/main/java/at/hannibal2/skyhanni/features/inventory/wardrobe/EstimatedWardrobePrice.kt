package at.hannibal2.skyhanni.features.inventory.wardrobe

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object EstimatedWardrobePrice {

    private val config get() = SkyHanniMod.feature.inventory.estimatedItemValues

    @SubscribeEvent
    fun onTooltip(event: LorenzToolTipEvent) {
        if (!isEnabled()) return

        val slot = WardrobeAPI.slots.firstOrNull {
            event.slot.slotNumber == it.inventorySlot && it.isInCurrentPage()
        } ?: return

        val lore = WardrobeAPI.createPriceLore(slot)
        if (lore.isEmpty()) return

        val tooltip = event.toolTip
        var index = 3

        tooltip.add(index++, "")
        tooltip.addAll(index, lore)
    }

    private fun isEnabled() =
        LorenzUtils.inSkyBlock && config.armor && WardrobeAPI.inWardrobe() && !WardrobeAPI.inCustomWardrobe

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "misc.estimatedIemValueArmor", "misc.estimatedItemValues.armor")
    }
}
