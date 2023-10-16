package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.*
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.cleanName
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.nameWithEnchantment
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import io.github.moulberry.notenoughupdates.events.SlotClickEvent
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class FandomWikiFromMenus {

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onSlotClick(event: SlotClickEvent) {
        val chestName = InventoryUtils.openInventoryName()

        if (chestName.isEmpty() || !isEnabled()) return

        val itemClickedStack = event.slot.stack
        val itemClickedName = itemClickedStack.displayName
        val itemInHand = InventoryUtils.getItemInHand() ?: return
        val itemInHandName = itemInHand.nameWithEnchantment ?: return
        val internalName = itemInHand.getInternalName().asString() ?: return

        if ((itemInHandName == "") || (event.slotId == 11 && itemClickedName.contains("Wiki Command") && chestName.contains("Wiki"))) {
            LorenzUtils.clickableChat("§e[SkyHanni] Click here to visit the Hypixel Skyblock Fandom Wiki!", "wiki")
        } else if (event.slotId == 15 && itemClickedName.contains("Wikithis Command") && chestName.contains("Wiki")) {
            LorenzUtils.clickableChat("§e[SkyHanni] Click here to search for the $itemInHandName §eon the Hypixel Skyblock Fandom Wiki!", "wiki $internalName")
        } else if (itemClickedStack.getLore().any { it.lowercase().contains("wiki") }) { //.lowercase() to match "Wiki!" and "wiki" lore lines in one fell swoops
            LorenzUtils.clickableChat("§e[SkyHanni] Click here to search for the $itemClickedName §eon the Hypixel Skyblock Fandom Wiki!", "wiki ${itemClickedName.removeColor()}")
        } else return
        event.isCanceled = true

    }
    private fun isEnabled() = SkyHanniMod.feature.commands.useFandomWiki
}