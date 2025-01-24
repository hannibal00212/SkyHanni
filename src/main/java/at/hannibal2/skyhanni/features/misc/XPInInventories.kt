package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.minecraft.ToolTipEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatchers
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.Minecraft

@SkyHanniModule
object XPInInventories {
    private val config get() = SkyHanniMod.feature.misc

    /**
     * REGEX-TEST: §310 Exp Levels
     * REGEX-TEST:§7Starting cost: §b350 XP Levels
     */
    private val xpLevelsPattern by RepoPattern.list(
        "misc.xp-in-inventory.exp-levels",
        "(?:§.)*(?<xp>\\d+) Exp Levels",
        "(?:§.)*Starting cost: §b(?<xp>\\d+) XP Levels",
    )

    @HandleEvent
    fun onToolTip(event: ToolTipEvent) {
        if (!isEnabled()) return

        var requiredXP = 0
        val indexOfCost = event.toolTip.indexOfFirst {
            xpLevelsPattern.matchMatchers(it) {
                requiredXP = group("xp").toInt()
            } != null
        }
        if (indexOfCost == -1) return

        val playerXP = Minecraft.getMinecraft().thePlayer.experienceLevel
        val color = if (playerXP >= requiredXP) "§a" else "§c"
        event.toolTip.add(indexOfCost + 1, "§7Your XP: $color${Minecraft.getMinecraft().thePlayer.experienceLevel}")
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.xpInInventory
}
