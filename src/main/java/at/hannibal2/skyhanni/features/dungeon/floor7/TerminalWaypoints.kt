package at.hannibal2.skyhanni.features.dungeon.floor7

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.features.dungeon.DungeonAPI
import at.hannibal2.skyhanni.features.dungeon.DungeonBossAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.getLorenzVec
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object TerminalWaypoints {

    private val config get() = SkyHanniMod.feature.dungeon

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return

        for (term in TerminalInfo.entries) {
            if (!term.highlight || !term.phase.isCurrent()) continue
            event.drawWaypointFilled(term.location, LorenzColor.GREEN.toColor(), seeThroughBlocks = true)
            event.drawDynamicText(term.location, term.text, 1.0)
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        TerminalInfo.resetTerminals()
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!inBoss()) return

        val playerName = DungeonBossAPI.goldorTerminalPattern.matchMatcher(event.message) {
            group("playerName")
        } ?: return

        val playerEntity = EntityUtils.getEntities<EntityPlayerMP>().find { it.name == playerName } ?: return
        val terminal = TerminalInfo.getClosestTerminal(playerEntity.getLorenzVec())
        terminal?.highlight = false
    }

    private fun inBoss() = DungeonAPI.inBossRoom && DungeonAPI.isOneOf("F7", "M7")

    private fun isEnabled() = inBoss() && config.terminalWaypoints
}
