package at.hannibal2.skyhanni.features.misc.pets

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.GuiRenderItemEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RenderUtils.drawSlotText
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getMaxPetLevel
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getPetCandyUsed
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getPetLevel
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object PetCandyUsedDisplay {

    private val config get() = SkyHanniMod.feature.misc.petCandy

    @SubscribeEvent
    fun onRenderItemOverlayPost(event: GuiRenderItemEvent.RenderOverlayEvent.GuiRenderItemPost) {
        val stack = event.stack ?: return
        if (!LorenzUtils.inSkyBlock || stack.stackSize != 1) return
        if (!config.showCandy) return

        if (config.hideOnMaxed) {
            if (stack.getPetLevel() == stack.getMaxPetLevel()) return
        }

        val petCandyUsed = stack.getPetCandyUsed() ?: return
        if (petCandyUsed == 0) return

        val stackTip = "§c$petCandyUsed"
        val x = event.x + 13
        val y = event.y + 1

        event.drawSlotText(x, y, stackTip, .9f)
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(22, "misc.petCandyUsed", "misc.petCandy.showCandy")
    }
}
