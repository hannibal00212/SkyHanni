package at.hannibal2.skyhanni.features.inventory.experimentationtable

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiRenderItemEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.drawSlotText
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.Minecraft
import net.minecraft.init.Items

@SkyHanniModule
object ExperimentationXPOverlay {

    private val patternGroup = RepoPattern.group("enchanting.experiments")

    /**
     * REGEX-TEST: §331k Enchanting Exp
     * REGEX-TEST: §3143k Enchanting Exp
     * REGEX-TEST: §350k Enchanting Exp
     * REGEX-TEST: §341k Enchanting Exp
     * REGEX-TEST: §3137k Enchanting Exp
     * REGEX-TEST: §3142k Enchanting Exp
     * REGEX-TEST: §3130k Enchanting Exp
     * REGEX-TEST: §36.5k Enchanting Exp
     * REGEX-TEST: §35.5k Enchanting Exp
     * REGEX-TEST: §33.5k Enchanting Exp
     */
    private val enchantingXPPattern by patternGroup.pattern(
        "enchantingxp",
        "§3(?<xp>[\\d.]+)k Enchanting Exp",
    )

    @HandleEvent
    fun onRenderItemOverlayPost(event: GuiRenderItemEvent.RenderOverlayEvent.GuiRenderItemPost) {
        if (!isEnabled()) return
        event.stack ?: return
        if (event.stack.item != Items.dye) return
        enchantingXPPattern.matchMatcher(event.stack.displayName) {
            val text = "${group("xp")}k"
            val stringWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(text)
            event.drawSlotText(event.x + 2 + stringWidth, event.y + 10, text, .6f)
        }
    }

    fun isEnabled(): Boolean {
        return ExperimentationTableApi.superpairInventory.isInside() &&
            SkyHanniMod.feature.inventory.experimentationTable.superpairsXPOverlay
    }
}
