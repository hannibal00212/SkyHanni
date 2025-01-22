package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.minecraft.ToolTipEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ClipboardUtils
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.StringUtils.firstLetterUppercase
import at.hannibal2.skyhanni.utils.StringUtils.removeColor

@SkyHanniModule
object CopyPlaytime {

    @HandleEvent(priority = HandleEvent.LOWEST)
    fun onTooltip(event: ToolTipEvent) {
        if (InventoryUtils.openInventoryName() != "Detailed /playtime") return
        if (event.slot.slotNumber != 4) return

        event.toolTip.add("")
        event.toolTip.add("§eClick to Copy!")
    }

    @HandleEvent
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (InventoryUtils.openInventoryName() != "Detailed /playtime") return
        if (event.slotId != 4) return
        if (event.clickedButton != 0) return

        event.cancel()
        val text = event.item?.getLore()?.toMutableList() ?: return

        val profile = HypixelData.profileName.firstLetterUppercase()
        text.add(0, "${LorenzUtils.getPlayerName()}'s - $profile Playtime Stats")

        ClipboardUtils.copyToClipboard(text.joinToString("\n") { it.removeColor() })
        ChatUtils.chat("Copied playtime stats into clipboard.")
    }
}
