package at.hannibal2.skyhanni.features.misc.pets

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.GuiRenderItemEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RenderUtils.drawSlotText
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getPetItem
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object PetItemDisplay {

    private val config get() = SkyHanniMod.feature.misc.pets

    @SubscribeEvent
    fun onRenderItemOverlayPost(event: GuiRenderItemEvent.RenderOverlayEvent.GuiRenderItemPost) {
        val stack = event.stack ?: return
        if (!LorenzUtils.inSkyBlock || stack.stackSize != 1) return
        if (config.petItemDisplay.isEmpty()) return

        val petItem = stack.getPetItem() ?: return
        val icon = config.petItemDisplay.firstOrNull { it.item == petItem }?.icon ?: return

        val width = (Minecraft.getMinecraft().fontRendererObj.getStringWidth(icon) * config.petItemDisplayScale).toInt()
        val x = event.x + 22 - width
        val y = event.y - 1

        event.drawSlotText(x, y, icon, config.petItemDisplayScale)
    }
}
