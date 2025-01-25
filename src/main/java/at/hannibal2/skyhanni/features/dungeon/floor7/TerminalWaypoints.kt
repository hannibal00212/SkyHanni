package at.hannibal2.skyhanni.features.dungeon.floor7

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.features.dungeon.DungeonApi
import at.hannibal2.skyhanni.features.dungeon.DungeonBossApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.getLorenzVec
import net.minecraft.entity.player.EntityPlayerMP

@SkyHanniModule
object TerminalWaypoints {

    private val config get() = SkyHanniMod.feature.dungeon

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled()) return

        for (term in TerminalInfo.entries) {
            if (!term.highlight || !term.phase.isCurrent()) continue
            event.drawWaypointFilled(term.location, LorenzColor.GREEN.toColor(), seeThroughBlocks = true)
            event.drawDynamicText(term.location, term.text, 1.0)
        }
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        TerminalInfo.resetTerminals()
    }

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (!inBoss()) return

        val playerName = DungeonBossApi.goldorTerminalPattern.matchMatcher(event.message) {
            group("playerName")
        } ?: return

        val playerEntity = EntityUtils.getEntities<EntityPlayerMP>().find { it.name == playerName } ?: return
        val terminal = TerminalInfo.getClosestTerminal(playerEntity.getLorenzVec())
        terminal?.highlight = false
    }

    private fun inBoss() = DungeonApi.inBossRoom && DungeonApi.isOneOf("F7", "M7")

    private fun isEnabled() = inBoss() && config.terminalWaypoints
}
